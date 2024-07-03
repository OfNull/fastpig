package com.ofnull.fastpig.connector.kafka.ratelimit;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.streaming.api.operators.StreamingRuntimeContext;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.streaming.connectors.kafka.config.OffsetCommitMode;
import org.apache.flink.streaming.connectors.kafka.internals.AbstractFetcher;
import org.apache.flink.streaming.connectors.kafka.internals.KafkaTopicPartition;
import org.apache.flink.util.SerializedValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * @author ofnull
 * @date 2024/4/24 16:56
 */
public class FlinkKafkaRateLimitedConsumer<T> extends FlinkKafkaConsumer<T> {
    private PartitionRateLimiter rateLimiter;

    public FlinkKafkaRateLimitedConsumer(String topic, DeserializationSchema<T> valueDeserializer, Properties props, PartitionRateLimiter rateLimiter) {
        super(topic, valueDeserializer, props);
        this.rateLimiter = rateLimiter;
    }

    public FlinkKafkaRateLimitedConsumer(String topic, KafkaDeserializationSchema<T> deserializer, Properties props, PartitionRateLimiter rateLimiter) {
        super(topic, deserializer, props);
        this.rateLimiter = rateLimiter;
    }

    public FlinkKafkaRateLimitedConsumer(List<String> topics, DeserializationSchema<T> deserializer, Properties props, PartitionRateLimiter rateLimiter) {
        super(topics, deserializer, props);
        this.rateLimiter = rateLimiter;
    }

    public FlinkKafkaRateLimitedConsumer(List<String> topics, KafkaDeserializationSchema<T> deserializer, Properties props, PartitionRateLimiter rateLimiter) {
        super(topics, deserializer, props);
        this.rateLimiter = rateLimiter;
    }

    public FlinkKafkaRateLimitedConsumer(Pattern subscriptionPattern, DeserializationSchema<T> valueDeserializer, Properties props, PartitionRateLimiter rateLimiter) {
        super(subscriptionPattern, valueDeserializer, props);
        this.rateLimiter = rateLimiter;
    }

    public FlinkKafkaRateLimitedConsumer(Pattern subscriptionPattern, KafkaDeserializationSchema<T> deserializer, Properties props, PartitionRateLimiter rateLimiter) {
        super(subscriptionPattern, deserializer, props);
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void open(Configuration configuration) throws Exception {
        super.open(configuration);
        if (rateLimiter != null) {
            rateLimiter.open(getRuntimeContext());
        }
    }

    @Override
    protected AbstractFetcher<T, ?> createFetcher(SourceContext<T> sourceContext, Map<KafkaTopicPartition, Long> assignedPartitionsWithInitialOffsets, SerializedValue<WatermarkStrategy<T>> watermarkStrategy, StreamingRuntimeContext runtimeContext, OffsetCommitMode offsetCommitMode, MetricGroup consumerMetricGroup, boolean useMetrics) throws Exception {

        // make sure that auto commit is disabled when our offset commit mode is ON_CHECKPOINTS;
        // this overwrites whatever setting the user configured in the properties
        super.adjustAutoCommitConfig(properties, offsetCommitMode);


        return new RateLimitedKafkaFetcher<>(
                sourceContext,
                assignedPartitionsWithInitialOffsets,
                watermarkStrategy,
                runtimeContext.getProcessingTimeService(),
                runtimeContext.getExecutionConfig().getAutoWatermarkInterval(),
                runtimeContext.getUserCodeClassLoader(),
                runtimeContext.getTaskNameWithSubtasks(),
                deserializer,
                properties,
                pollTimeout,
                runtimeContext.getMetricGroup(),
                consumerMetricGroup,
                useMetrics,
                rateLimiter);
    }

    @Override
    public void close() throws Exception {
        super.close();
        if (rateLimiter != null) {
            rateLimiter.close();
        }
    }

}
