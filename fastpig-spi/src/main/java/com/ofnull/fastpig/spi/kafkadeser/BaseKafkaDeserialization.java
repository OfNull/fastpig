package com.ofnull.fastpig.spi.kafkadeser;

import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;

import java.util.Map;

/**
 * @author ofnull
 * @date 2022/8/18 18:30
 */
public interface BaseKafkaDeserialization<T> extends KafkaDeserializationSchema<T> {

    void configure(boolean attachMeta);

}
