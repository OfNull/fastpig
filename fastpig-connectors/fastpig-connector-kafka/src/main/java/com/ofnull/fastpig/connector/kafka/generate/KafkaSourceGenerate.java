package com.ofnull.fastpig.connector.kafka.generate;

import com.ofnull.fastpig.common.finder.ServiceLoaderHelper;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.common.utils.ValidatorUtil;
import com.ofnull.fastpig.connector.kafka.config.KafkaSourceConfig;
import com.ofnull.fastpig.connector.kafka.ratelimit.FlinkKafkaRateLimitedConsumer;
import com.ofnull.fastpig.connector.kafka.ratelimit.PartitionRateLimiter;
import com.ofnull.fastpig.connector.kafka.ratelimit.SinglePartitionRateLimiter;
import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.kafkadeser.BaseKafkaDeserialization;
import com.ofnull.fastpig.spi.instance.IInstanceGenerate;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.config.StartupMode;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author ofnull
 * @date 2022/2/14 11:19
 */
@PigType("kafkaSource")
public class KafkaSourceGenerate implements IInstanceGenerate<SourceFunction<Map<String, Object>>> {
    private KafkaSourceConfig config;

    @Override
    public void open(Map<String, Object> cfg) {
        this.config = JsonUtil.tryRead(JsonUtil.toJsonString(cfg), KafkaSourceConfig.class);
        ValidatorUtil.validate(this.config);
    }

    @Override
    public SourceFunction<Map<String, Object>> generateInstance() {
        Properties prop = config.getProperties();
        BaseKafkaDeserialization<Map<String, Object>> kafkaDeserialization = ServiceLoaderHelper.loadServices(BaseKafkaDeserialization.class, config.getFormat());
        kafkaDeserialization.configure(config.getAttachMeta());
        FlinkKafkaConsumer<Map<String, Object>> consumer;
        //限流判断
        if (config.getRateLimit() != null && config.getRateLimit().getEnabled()) {
            config.reconfigureRateLimitConfig(config.getRateLimit(), config.getRateLimit().getProperties());
            PartitionRateLimiter partitionRateLimiter = new SinglePartitionRateLimiter(config.getTopic(), config.getRateLimit());
            consumer = new FlinkKafkaRateLimitedConsumer<>(config.getTopic(), kafkaDeserialization, prop, partitionRateLimiter);
        }else {
            consumer = new FlinkKafkaConsumer<>(config.getTopic(), kafkaDeserialization, prop);
        }
        setStartupMode(consumer);
        return consumer;
    }

    private void setStartupMode(FlinkKafkaConsumer consumer) {
        if (Objects.nonNull(config.getStartupMode())) {
            if (StartupMode.EARLIEST == config.getStartupMode()) {
                consumer.setStartFromEarliest();
            } else if (StartupMode.LATEST == config.getStartupMode()) {
                consumer.setStartFromLatest();
            } else if (StartupMode.TIMESTAMP == config.getStartupMode()) {
                config.checkDateTime();
                long ts = config.parseDateTime();
                consumer.setStartFromTimestamp(ts);
            } else if (StartupMode.GROUP_OFFSETS == config.getStartupMode()) {
                consumer.setStartFromGroupOffsets();
            }
        }
    }


}
