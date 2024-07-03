package com.ofnull.fastpig.connector.kafka.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ofnull.fastpig.connector.kafka.ratelimit.SinglePartitionRateLimiter;
import org.apache.flink.shaded.guava30.com.google.common.collect.Maps;
import org.apache.flink.streaming.connectors.kafka.config.StartupMode;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/**
 * @author ofnull
 * @date 2022/2/14 11:35
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaSourceConfig {
    public static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
    public static final String GROUP_ID = "group.id";
    public static final String KEY_DESERIALIZER = "key.deserializer";
    public static final String VALUE_DESERIALIZER = "value.deserializer";
    public static final String AUTO_OFFSET_RESET = "auto.offset.reset";
    public static final String ENABLE_AUTO_COMMIT = "enable.auto.commit";
    public static final String AUTO_COMMIT_INTERVAL_MS = "auto.commit.interval.ms";

    @NotEmpty
    private String topic;

    private StartupMode startupMode;
    private String startupArgs;
    @NotEmpty
    private String format;


    private Boolean attachMeta = false;

    private Map<String, Object> properties;

    /**
     * 消费限流配置
     */
    private RateLimitConfig rateLimit;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }


    public StartupMode getStartupMode() {
        return startupMode;
    }

    public void setStartupMode(StartupMode startupMode) {
        this.startupMode = startupMode;
    }

    public String getStartupArgs() {
        return startupArgs;
    }

    public void setStartupArgs(String startupArgs) {
        this.startupArgs = startupArgs;
    }

    public Boolean getAttachMeta() {
        return attachMeta;
    }

    public void setAttachMeta(Boolean addMeta) {
        attachMeta = addMeta;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public RateLimitConfig getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimitConfig rateLimit) {
        this.rateLimit = rateLimit;
    }

    public long parseDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(startupArgs);
            return date.getTime();
        } catch (ParseException e) {
            throw new RuntimeException("KafkaSourceConfig  Param.dateTime parse Exception! message:" + e.getMessage());
        }
    }

    public void checkDateTime() {
        if (Objects.isNull(startupArgs)) {
            throw new RuntimeException("KafkaSourceConfig ConsumerPoint=TIMESTAMP Model Param.dateTime Is Not Null!");
        }
        Pattern pattern = Pattern.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$");
        if (!pattern.matcher(startupArgs).matches()) {
            throw new RuntimeException("KafkaSourceConfig ConsumerPoint=TIMESTAMP Model Param.dateTime incorrect format! example: 1970-12-30 10:12:50");
        }
    }

    public Properties getProperties() {
        Properties config = new Properties();
        if (Objects.isNull(this.properties)) {
            return config;
        }
        config.putAll(this.properties);
        return config;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }


    public static class RateLimitConfig implements Serializable {
        //是否开启
        @NotNull(message = "enabled not Null!")
        private Boolean enabled = false;

        @NotNull(message = "reteLimit not Null!")
        private Long reteLimit = 60000L;

        @NotNull(message = "timeUnit not Null!")
        private TimeUnit timeUnit = TimeUnit.MINUTES;

        private Map<String, Object> properties;

        @NotNull(message = "timeDuration not Null!")
        private Integer timeDuration = 1;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Long getReteLimit() {
            return reteLimit;
        }

        public void setReteLimit(Long reteLimit) {
            this.reteLimit = reteLimit;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public void setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }

        public Integer getTimeDuration() {
            return timeDuration;
        }

        public void setTimeDuration(Integer timeDuration) {
            this.timeDuration = timeDuration;
        }
    }


    public void reconfigureRateLimitConfig(RateLimitConfig rateLimitConfig, Map<String, Object> properties) {
        if (rateLimitConfig.getProperties() == null) {
            rateLimitConfig.setProperties(Maps.newHashMap());
        }
        Map<String, Object> rateLimitProperties = rateLimitConfig.getProperties();
        Map<String, Object> limitProperties = Maps.newHashMap(properties);
        limitProperties.putAll(rateLimitProperties);
        if (!rateLimitProperties.containsKey(AUTO_OFFSET_RESET)) {
            limitProperties.put(AUTO_OFFSET_RESET, "latest");
        }
        if (!rateLimitProperties.containsKey(KEY_DESERIALIZER)) {
            limitProperties.put(KEY_DESERIALIZER, StringDeserializer.class.getName());
        }
        if (!rateLimitProperties.containsKey(VALUE_DESERIALIZER)) {
            limitProperties.put(VALUE_DESERIALIZER, StringDeserializer.class.getName());
        }
        if (!rateLimitProperties.containsKey(ENABLE_AUTO_COMMIT)) {
            limitProperties.put(ENABLE_AUTO_COMMIT, true);
        }
        if (!rateLimitProperties.containsKey(AUTO_COMMIT_INTERVAL_MS)) {
            limitProperties.put(AUTO_COMMIT_INTERVAL_MS, 5000);
        }
        limitProperties.remove(GROUP_ID);
        rateLimitConfig.setProperties(limitProperties);
    }

}
