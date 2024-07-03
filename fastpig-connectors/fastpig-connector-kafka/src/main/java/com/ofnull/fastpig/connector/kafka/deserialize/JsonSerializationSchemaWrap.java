package com.ofnull.fastpig.connector.kafka.deserialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ofnull.fastpig.common.utils.JsonPathSupportedMap;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.spi.kafkadeser.BaseKafkaSerialization;
import com.ofnull.fastpig.spi.kafkadeser.KafkaFormatOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.streaming.connectors.kafka.KafkaContextAware;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Map;


/**
 * @author ofnull
 * @date 2024/5/28 17:09
 */
public class JsonSerializationSchemaWrap implements BaseKafkaSerialization<Map<String, Object>>, KafkaContextAware<Map<String, Object>> {
    public static final Logger LOG = LoggerFactory.getLogger(JsonSerializationSchemaWrap.class);

    private FlinkKafkaPartitioner<Map<String, Object>> partitioner;
    private int[] partitions;
    private int parallelInstanceId;
    private int numParallelInstances;

    private KafkaFormatOptions formatOptions;
    private String topic;


    @Override
    public void configure(String topic, FlinkKafkaPartitioner<Map<String, Object>> partitioner, KafkaFormatOptions formatOptions) {
        this.formatOptions = formatOptions;
        this.topic = topic;
        this.partitioner = partitioner;
    }

    @Override
    public void open(SerializationSchema.InitializationContext context) throws Exception {
        if (partitioner != null) {
            partitioner.open(parallelInstanceId, numParallelInstances);
        }
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(Map<String, Object> element, @Nullable Long timestamp) {
        final Integer partition;
        byte[] serializeKey = serializeKey(element);
        byte[] serializeValue = serializeValue(element);
        if (partitioner != null) {
            partition = partitioner.partition(element, serializeKey, serializeValue, topic, partitions);
        } else {
            partition = null;
        }

        return new ProducerRecord<byte[], byte[]>(getTargetTopic(element), partition, null, serializeValue);
    }

    private byte[] serializeKey(Map<String, Object> element) {
        if (StringUtils.isNotBlank(formatOptions.getKeyField())) {
            JsonPathSupportedMap jsonPathMap = new JsonPathSupportedMap(formatOptions.getJsonPathSupport(), element);
            String key = jsonPathMap.getString(formatOptions.getKeyField());
            if (StringUtils.isNotBlank(key)) {
                return key.getBytes(StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private byte[] serializeValue(Map<String, Object> element) {
        try {
            return JsonUtil.objectMapper().writeValueAsBytes(element);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json serialize failed", e);
        }
    }

    @Override
    public String getTargetTopic(Map<String, Object> element) {
        String targetTopic = this.topic;
        String topicFiled = formatOptions.getTopicField();
        if (StringUtils.isNotBlank(topicFiled)) {
            JsonPathSupportedMap jsonPathMap = new JsonPathSupportedMap(formatOptions.getJsonPathSupport(), element);
            targetTopic = jsonPathMap.getString(topicFiled);
            if (StringUtils.isBlank(targetTopic)) {
                targetTopic = this.topic;
                LOG.warn("user opened use message topic, bug result null, use default:{}", topic);
            }
        }
        return targetTopic;
    }

    @Override
    public void setPartitions(int[] partitions) {
        this.partitions = partitions;
    }

    @Override
    public void setParallelInstanceId(int parallelInstanceId) {
        this.parallelInstanceId = parallelInstanceId;
    }

    @Override
    public void setNumParallelInstances(int numParallelInstances) {
        this.numParallelInstances = numParallelInstances;
    }
}
