package com.ofnull.fastpig.spi.kafkadeser;

import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;

import java.util.Map;

/**
 * @author ofnull
 * @date 2024/5/28 17:06
 */
public interface BaseKafkaSerialization<T> extends KafkaSerializationSchema<T> {

    void configure(String topic, FlinkKafkaPartitioner<Map<String, Object>> partitioner, KafkaFormatOptions options);

}
