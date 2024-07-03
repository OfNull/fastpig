package com.ofnull.fastpig.connector.elasticsearch.bulkloader;

import com.google.common.base.Preconditions;
import com.ofnull.fastpig.common.finder.ServiceLoaderHelper;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.spi.bulkloader.BaseBulkLoader;
import com.ofnull.fastpig.spi.bulkloader.ExecBatch;
import com.ofnull.fastpig.spi.bulkloader.IPreparedWrite;
import com.ofnull.fastpig.spi.columntransform.IKeyTransform;
import com.ofnull.fastpig.spi.metainfo.ColumnMetaInfo;
import com.ofnull.fastpig.spi.metainfo.TableMetaInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author ofnull
 * @date 2024/6/12 14:34
 */
public class ElasticsearchBulkLoader extends BaseBulkLoader<ElasticsearchBulkLoader.EsRequestWrap> {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchBulkLoader.class);

    private RestClientBuilder restClient;
    private RestHighLevelClient highLevelClient;
    private Map<String, IKeyTransform> storageTransforms;
    private TableMetaInfo tableMetaInfo;

    @Override
    public void init(int parallelism, TableMetaInfo tableMetaInfo) throws Exception {
        this.tableMetaInfo = tableMetaInfo;
        ElasticsearchConfig elasticsearchConfig = JsonUtil.tryRead(tableMetaInfo.getDatasourceConnectInfo(), ElasticsearchConfig.class);
        if (elasticsearchConfig.getMaxConnPerRoute() > parallelism) {
            LOG.info("Bad config, es {} max connection per route {} < parallelism {}, reset to {}",
                    elasticsearchConfig.getMaxConnPerRoute(), parallelism, parallelism);
            elasticsearchConfig.setMaxConnPerRoute(parallelism);
        }
        restClient = restClientBuilder(elasticsearchConfig);
        highLevelClient = new RestHighLevelClient(restClient);
        Preconditions.checkArgument(
                existsIndex(tableMetaInfo), tableMetaInfo.getTable() + " index does not exist.");
    }

    private boolean existsIndex(TableMetaInfo tableInfo) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(tableInfo.getTable());
        boolean exists = highLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if (exists == Boolean.FALSE) {
            GetAliasesRequest getAliasesRequest = new GetAliasesRequest(tableInfo.getTable());
            exists = highLevelClient.indices().existsAlias(getAliasesRequest, RequestOptions.DEFAULT);
        }
        return exists;
    }

    private RestClientBuilder restClientBuilder(ElasticsearchConfig config) {
        List<HttpHost> httpHosts = config.getUrls().stream()
                .map(HttpHost::create)
                .collect(Collectors.toList());

        RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[0]));
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            if (isNotBlank(config.getUser()) && isNotBlank(config.getPassword())) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(config.getUser(), config.getPassword()));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            return httpClientBuilder.setMaxConnTotal(config.getMaxConnTotal()).setMaxConnPerRoute(config.getMaxConnPerRoute());
        });
        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder.setConnectTimeout(config.getConnectTimeout()).setSocketTimeout(config.getSocketTimeout())
        );
        builder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(Node node) {
                LOG.warn("Node failure: host {}, port {}, node name: {}", node.getHost().getHostName(), node.getHost().getPort(), node.getName());
            }
        });
        return builder;
    }

    @Override
    public void doBatchWrite(int partition, ExecBatch execBatch) throws Exception {

        List<DocWriteRequest> dataList = (List<DocWriteRequest>) execBatch.getDataList();
        if (dataList == null || dataList.isEmpty()) {
            LOG.debug("Data list is null or empty. Skipping batch processing.");
            return;
        }
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(dataList.toArray(new DocWriteRequest[0]));
        bulkRequest.add(dataList.toArray(new DocWriteRequest[0]));
        BulkResponse bulkResponse = highLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        this.handleBulkFailures(bulkResponse);

    }

    private void handleBulkFailures(BulkResponse bulkResponse) {
        for (BulkItemResponse bulkItemResponse : bulkResponse) {
            if (bulkItemResponse.isFailed()) {
                if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
                    LOG.debug("Create operation failed but will be ignored: " + bulkItemResponse.getFailureMessage());
                    continue;
                }
                throw new RuntimeException("Es bulk request failed: " + bulkResponse.buildFailureMessage());
            }
        }
    }

    @Override
    public IKeyTransform finderStorageTransform(String name) {
        if (storageTransforms == null) {
            storageTransforms = new HashMap<>();
        }
        return storageTransforms.putIfAbsent(name, ServiceLoaderHelper.loadServices(IKeyTransform.class, name));
    }

    @Override
    public EsRequestWrap insert(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) {
        EventParseResult parseResult = dataParse(event, columnMetaInfos);
        if (parseResult.getCounter() == 0) {
            return new EsRequestWrap(null);
        }
        String pk = StringUtils.join(parseResult.getPks(), SEPARATOR);
        System.out.println("----------------" + pk);
        UpdateRequest request = new UpdateRequest(tableName(), pk);
        request.doc(parseResult.getSource(), XContentType.JSON);
        request.docAsUpsert(true);
        request.detectNoop(false); //关闭 无操作（no-op）检查
        return new EsRequestWrap(request);
    }


    @Override
    public EsRequestWrap insertIgnore(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) {
        EventParseResult parseResult = dataParse(event, columnMetaInfos);
        if (parseResult.getCounter() == 0) {
            return new EsRequestWrap(null);
        }
        String pk = StringUtils.join(parseResult.getPks(), SEPARATOR);
        IndexRequest request = new IndexRequest(tableName());
        request.id(pk);
        request.source(parseResult.getSource(), XContentType.JSON);
        request.opType(DocWriteRequest.OpType.CREATE); // 仅在文档不存在时创建
        return new EsRequestWrap(request);
    }

    @Override
    public EsRequestWrap update(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) {
        EventParseResult parseResult = dataParse(event, columnMetaInfos);
        if (parseResult.getCounter() == 0) {
            return new EsRequestWrap(null);
        }
        String pk = StringUtils.join(parseResult.getPks(), SEPARATOR);
        UpdateRequest request = new UpdateRequest(tableName(), pk);
        request.doc(parseResult.getSource(), XContentType.JSON);
        request.docAsUpsert(true);
        return new EsRequestWrap(request);
    }

    @Override
    public EsRequestWrap delete(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) {
        EventParseResult parseResult = dataParse(event, columnMetaInfos);
        String pk = StringUtils.join(parseResult.getPks(), SEPARATOR);
        DeleteRequest request = new DeleteRequest(tableName(), pk);
        return new EsRequestWrap(request);
    }

    @Override
    public EsRequestWrap upsert(Map<String, Object> event, List<ColumnMetaInfo> columnMetaInfos) {
        return insert(event, columnMetaInfos);
    }

    @Override
    public void close() throws IOException {
        if (highLevelClient != null) {
            highLevelClient.close();
        }
    }

    private String tableName() {
        return tableMetaInfo.getTable();
    }

    public static class EsRequestWrap implements IPreparedWrite {

        private DocWriteRequest request;

        public EsRequestWrap(DocWriteRequest request) {

            this.request = request;
        }

        @Override
        public boolean isNotEmpty() {
            return request != null;
        }


        @Override
        public DocWriteRequest getData() {
            return request;
        }
    }
}
