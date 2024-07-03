package com.ofnull.fastpig.common.metainfo;

import com.ofnull.fastpig.common.jdbc.SqlExecutor;
import com.ofnull.fastpig.spi.jdbc.IJdbcConnection;
import com.ofnull.fastpig.spi.metainfo.IMetaLoader;
import com.ofnull.fastpig.spi.metainfo.JobRecordProcessMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author ofnull
 * @date 2024/6/20
 */
public class JobRecordProcessMetaLoader implements IMetaLoader<List<JobRecordProcessMetaInfo>> {
    private static final Logger LOG = LoggerFactory.getLogger(TableMetaLoader.class);
    private IJdbcConnection connection;
    private String jobName;
    private String operator;

    public JobRecordProcessMetaLoader(IJdbcConnection connection, String jobName, String operator) {
        this.connection = connection;
        this.jobName = jobName;
        this.operator = operator;
    }

    private String querySql() {
        String sql = String.format("select `id`, `conditions` as `match_rules` , `yes_processor`, `yes_config`, `no_processor`, `no_config`, `orders`, `sub_flow`, `fail_policy`\n" +
                " from `meta_job_process` \n" +
                " where `job_name` = '%s' and `operator` = '%s'\n" +
                " order by `orders`, `id`;", jobName, operator);
        return sql;
    }

    @Override
    public List<JobRecordProcessMetaInfo> loader() throws Exception {
        List<JobRecordProcessMetaInfo> processMetaInfos = SqlExecutor.listQuery(connection, querySql(), JobRecordProcessMetaInfo.class);
        return processMetaInfos;
    }
}
