package com.ofnull.fastpig.run;

import com.ofnull.fastpig.common.cli.DefaultCli;
import com.ofnull.fastpig.common.constant.ConfigurationConstant;
import com.ofnull.fastpig.common.finder.ServiceLoaderHelper;
import com.ofnull.fastpig.common.job.CheckpointInfo;
import com.ofnull.fastpig.common.job.JobConfiguration;
import com.ofnull.fastpig.common.job.JobInfo;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.common.utils.ValidatorUtil;
import com.ofnull.fastpig.spi.configure.IConfigure;
import com.ofnull.fastpig.spi.instance.IInstanceGenerate;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.ofnull.fastpig.common.constant.ConfigurationConstant.*;


/**
 * @author ofnull
 * @date 2022/2/10 17:50
 */
public class SimpleStartSetUp {
    public static final Logger LOG = LoggerFactory.getLogger(SimpleStartSetUp.class);

    public static JobConfiguration entry(String[] args) throws Exception {
        JobConfiguration context = new JobConfiguration();
        CommandLine commandLine = parseArgs(args);
        Map<String, Object> cfg = getConfigure(commandLine);
        context.setCfgs(cfg);

        Map<String, Object> jobCfg = (Map<String, Object>) cfg.get(ConfigurationConstant.JOB);
        String jobCfgJson = JsonUtil.toJsonString(jobCfg);
        JobInfo jobInfo = JsonUtil.objectMapper().readValue(jobCfgJson, JobInfo.class);
        ValidatorUtil.validate(jobInfo);

        StreamExecutionEnvironment env = createStreamExecutionEnvironment(jobInfo, commandLine);
        configurationEnvironment(env, jobInfo);
        env.getConfig().setGlobalJobParameters(context);
        context.setEnv(env);
        return context;
    }

    public static DataStream<Map<String, Object>> addSource(JobConfiguration context) throws Exception {
        StreamExecutionEnvironment env = context.getEnv();
        List<Map<String, Object>> sources = (List<Map<String, Object>>) context.getCfgs().get(ConfigurationConstant.SOURCES);
        DataStream<Map<String, Object>> streamSource = null;
        for (Map<String, Object> sourceCfg : sources) {
            String type = (String) sourceCfg.get(TYPE);
            try {
                IInstanceGenerate sourceInstance = ServiceLoaderHelper.loadServices(IInstanceGenerate.class, type);
                sourceInstance.open(sourceCfg);
                SourceFunction<Map<String, Object>> sourceFunction = (SourceFunction<Map<String, Object>>) sourceInstance.generateInstance();
                DataStream<Map<String, Object>> currentStreamSource = env.addSource(sourceFunction).uid(String.valueOf(sourceCfg.getOrDefault(UID, UUID.randomUUID().toString()))).name(String.valueOf(sourceCfg.getOrDefault(NAME, "source")));
                if (Objects.isNull(streamSource)) {
                    streamSource = currentStreamSource;
                } else {
                    streamSource.join(currentStreamSource);
                }
            } catch (Exception e) {
                throw e;
            }
        }
        return streamSource;
    }

    public static CommandLine parseArgs(String[] args) throws ParseException {
        Options ops = new Options();
        DefaultCli.addOption(ops);
        CommandLine commandLine = new DefaultParser().parse(ops, args);
        return commandLine;
    }

    public static Map<String, Object> getConfigure(CommandLine commandLine) throws Exception {
        IConfigure configure = ServiceLoaderHelper.loadServices(IConfigure.class, DefaultCli.getOption(commandLine, DefaultCli.type, "local"));
        Map<String, Object> cfg = configure.readCfg(commandLine);
        return cfg;
    }

    public static StreamExecutionEnvironment createStreamExecutionEnvironment(JobInfo jobInfo, CommandLine commandLine) {
        String env = DefaultCli.getOption(commandLine, DefaultCli.env, "qa");
        if (jobInfo.isEnableLocalWeb() && env.equals("qa")) {
            Configuration configuration = new Configuration();
            configuration.setInteger(RestOptions.PORT, jobInfo.getWebPort());
            return StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        }
        return StreamExecutionEnvironment.getExecutionEnvironment();
    }

    public static void configurationEnvironment(StreamExecutionEnvironment env, JobInfo jobCfg) {
        configurationCheckpoint(env, jobCfg);
        configurationRestartStrategy(env, jobCfg);
        configurationParallelism(env, jobCfg);
    }

    public static void configurationParallelism(StreamExecutionEnvironment env, JobInfo info) {
        Optional.ofNullable(info.getParallelism()).ifPresent(v -> env.setParallelism(v));
        Optional.ofNullable(info.getMaxParallelism()).ifPresent(v -> env.setMaxParallelism(v));
    }

    public static void configurationCheckpoint(StreamExecutionEnvironment env, JobInfo jobCfg) {
        CheckpointInfo cp = jobCfg.getCheckpoint();
        if (cp.getEnable()) {
            Optional.ofNullable(cp.getInterval()).ifPresent(v -> env.getCheckpointConfig().setCheckpointInterval(v));
            Optional.ofNullable(cp.getBetweenInterval()).ifPresent(v -> env.getCheckpointConfig().setMinPauseBetweenCheckpoints(v));
            Optional.ofNullable(cp.getTimeout()).ifPresent(v -> env.getCheckpointConfig().setCheckpointTimeout(v));
            Optional.ofNullable(cp.getConcurrent()).ifPresent(v -> env.getCheckpointConfig().setMaxConcurrentCheckpoints(v));
            Optional.ofNullable(cp.getCleanup()).ifPresent(v -> env.getCheckpointConfig().setExternalizedCheckpointCleanup(v));
            Optional.ofNullable(cp.getCheckpointingMode()).ifPresent(v -> env.getCheckpointConfig().setCheckpointingMode(v));
        }
    }

    public static void configurationRestartStrategy(StreamExecutionEnvironment env, JobInfo jobCfg) {
        Optional.ofNullable(jobCfg.getRestart()).ifPresent(v -> env.setRestartStrategy(v.getRestartStrategyConfiguration()));
    }

    public static void executorJob(StreamExecutionEnvironment env, String name) throws Exception {
        env.execute(name);
    }


}
