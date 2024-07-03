package com.ofnull.fastpig.common.job;

import com.ofnull.fastpig.common.constant.ConfigurationConstant;
import com.ofnull.fastpig.common.finder.ServiceLoaderHelper;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.common.utils.ValidatorUtil;
import com.ofnull.fastpig.spi.instance.IInstanceGenerate;
import com.ofnull.fastpig.spi.job.IJobConfiguration;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.ofnull.fastpig.common.constant.ConfigurationConstant.TYPE;

/**
 * @author ofnull
 * @date 2024/6/17 14:59
 */
public class JobConfiguration extends ExecutionConfig.GlobalJobParameters implements IJobConfiguration, Serializable {
    private Map<String, Object> cfgs = new HashMap<>();
    private transient StreamExecutionEnvironment env;

    @Override
    public <T> T convertConfigToEntity(String key, Class<T> clazz) throws Exception {
        Object obj = cfgs.get(key);
        if (Objects.isNull(obj)) {
            throw new IllegalArgumentException("Job Config not found " + key);
        }
        if (obj instanceof Map == false) {
            return JsonUtil.convert(obj, clazz);
        }
        Map<String, Object> config = (Map<String, Object>) obj;
        T configValue = JsonUtil.convert(config, clazz);
        ValidatorUtil.validate(configValue);
        return configValue;
    }

    public <T> T generateInstance(String key) {
        Map<String, Object> cfg = (Map<String, Object>) cfgs.get(key);
        String type = (String) cfg.get(TYPE);
        IInstanceGenerate<T> instance = ServiceLoaderHelper.loadServices(IInstanceGenerate.class, type);
        instance.open(cfg);
        return instance.generateInstance();
    }


    public <T> T getRequiredConfig(String key, T defaultValue) {
        T v = getConfig(key);
        if (v == null) {
            return defaultValue;
        }
        return v;
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key) {
        try {
            return (T) cfgs.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public JobInfo getJobInfo() throws Exception {
        JobInfo jobInfo = JsonUtil.convert(cfgs.get(ConfigurationConstant.JOB), JobInfo.class);
        return jobInfo;
    }


    public Map<String, Object> getCfgs() {
        return cfgs;
    }

    public void setCfgs(Map<String, Object> cfgs) {
        this.cfgs = cfgs;
    }

    public StreamExecutionEnvironment getEnv() {
        return env;
    }

    public void setEnv(StreamExecutionEnvironment env) {
        this.env = env;
    }
}
