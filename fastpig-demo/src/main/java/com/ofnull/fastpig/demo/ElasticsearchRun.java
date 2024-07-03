package com.ofnull.fastpig.demo;

import com.ofnull.fastpig.common.jdbc.JdbcConnectionConfig;
import com.ofnull.fastpig.common.jdbc.SimpleJdbcConnection;
import com.ofnull.fastpig.common.job.JobConfiguration;
import com.ofnull.fastpig.common.metainfo.JobRecordProcessMetaLoader;
import com.ofnull.fastpig.common.metainfo.TableConfig;
import com.ofnull.fastpig.common.metainfo.TableMetaLoader;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.connector.elasticsearch.bulkloader.ElasticsearchBulkLoader;
import com.ofnull.fastpig.run.SimpleStartSetUp;
import com.ofnull.fastpig.spi.bulkloader.BaseBulkLoader;
import com.ofnull.fastpig.spi.bulkloader.ExecBatch;
import com.ofnull.fastpig.spi.bulkloader.IPreparedWrite;
import com.ofnull.fastpig.spi.bulkloader.WriteOperateEnum;
import com.ofnull.fastpig.spi.metainfo.JobRecordProcessMetaInfo;
import com.ofnull.fastpig.spi.metainfo.MatchRule;
import com.ofnull.fastpig.spi.metainfo.TableMetaInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ofnull
 * @date 2024/6/14 19:04
 */
public class ElasticsearchRun {
    public static void main(String[] args) throws Exception {
        JobConfiguration entry = SimpleStartSetUp.entry(args);
//        DataStream<Map<String, Object>> dataStream = SimpleStartSetUp.addSource(entry);

        Map<String, Object> metaInfoCfg = (Map<String, Object>) entry.getCfgs().get("metaInfo");
        JdbcConnectionConfig jdbcConfig = JsonUtil.convert(metaInfoCfg, JdbcConnectionConfig.class);
        SimpleJdbcConnection jdbcConnection = new SimpleJdbcConnection(jdbcConfig);
        TableConfig tableConfig = JsonUtil.convert(entry.getCfgs().get("tableInfo"), TableConfig.class);
        TableMetaLoader tableMetaLoader = new TableMetaLoader(jdbcConnection, tableConfig);
        TableMetaInfo tableMetaInfo = tableMetaLoader.loader();

        JobRecordProcessMetaLoader recordProcessMetaLoader = new JobRecordProcessMetaLoader(jdbcConnection, "test_job", "simple");
        List<JobRecordProcessMetaInfo> processMetaInfos = recordProcessMetaLoader.loader();
        MatchRule matchRule = processMetaInfos.get(0).getMatchRules().get("r1").get(0);
        System.out.println("");

        BaseBulkLoader baseBulkLoader = new ElasticsearchBulkLoader();
        baseBulkLoader.init(1, tableMetaInfo);

        Map<String, Object> event = new HashMap<>();
        event.put("bosId", "983991499");
        event.put("sChannelId", "741258");
        event.put("sChannelType", "146");
        event.put("updateTime", System.currentTimeMillis());
        IPreparedWrite preparedWrite = baseBulkLoader.prepareWrite(1, WriteOperateEnum.UPSERT, event, tableMetaInfo.getColumnMetadata());

        ExecBatch execBatch = new ExecBatch();
        execBatch.getDataList().add(preparedWrite.getData());
        baseBulkLoader.doBatchWrite(1, execBatch);
    }
}
