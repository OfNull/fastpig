package com.ofnull.fastpig.spi.connector.kafka;

import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;

/**
 * @author ofnull
 * @date 2024/6/6 14:47
 */
public abstract class BaseKafkaPartitioner<T> extends FlinkKafkaPartitioner<T> {
}
