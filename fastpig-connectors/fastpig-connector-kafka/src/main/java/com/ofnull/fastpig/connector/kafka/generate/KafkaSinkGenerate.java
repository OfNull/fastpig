package com.ofnull.fastpig.connector.kafka.generate;

import com.ofnull.fastpig.common.finder.ServiceLoaderHelper;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.common.utils.PropertiesUtil;
import com.ofnull.fastpig.common.utils.ValidatorUtil;
import com.ofnull.fastpig.connector.kafka.config.KafkaSinkConfig;
import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.connector.kafka.BaseKafkaPartitioner;
import com.ofnull.fastpig.spi.kafkadeser.BaseKafkaSerialization;
import com.ofnull.fastpig.spi.instance.IInstanceGenerate;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Map;
import java.util.Properties;

/**
 * @author ofnull
 */
@PigType("KafkaSink")
public class KafkaSinkGenerate implements IInstanceGenerate<SinkFunction<Map<String, Object>>> {
    private KafkaSinkConfig config;

    @Override
    public void open(Map<String, Object> cfg) {
        this.config = JsonUtil.tryRead(JsonUtil.toJsonString(cfg), KafkaSinkConfig.class);
        ValidatorUtil.validate(this.config);
    }

    @Override
    public SinkFunction<Map<String, Object>> generateInstance() {
        Properties properties = PropertiesUtil.toProperties(config.getProperties());
        BaseKafkaPartitioner<Map<String, Object>> partitioner = ServiceLoaderHelper.loadServices(BaseKafkaPartitioner.class, config.getPartitioner());
        BaseKafkaSerialization<Map<String, Object>> serialization = ServiceLoaderHelper.loadServices(BaseKafkaSerialization.class, config.getFormat());
        serialization.configure(config.getTopic(), partitioner, config.getFormatOptions());
        FlinkKafkaProducer<Map<String, Object>> kafkaProducer = new FlinkKafkaProducer<Map<String, Object>>(config.getTopic(),
                serialization, properties, FlinkKafkaProducer.Semantic.EXACTLY_ONCE);
        return kafkaProducer;
    }


}
