package com.ofnull.fastpig.connector.kafka.deserialize;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.spi.kafkadeser.BaseKafkaDeserialization;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.metrics.MetricGroup;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

/**
 * @author ofnull
 * @date 2022/2/14 15:15
 */
public class JsonDeserializationSchemaWrap implements BaseKafkaDeserialization<Map<String, Object>> {
    private MetricGroup metricGroup;
    private boolean attachMeta = false;


    @Override
    public void configure(boolean attachMeta) {
        this.attachMeta = attachMeta;
    }


    @Override
    public void open(DeserializationSchema.InitializationContext context) throws Exception {
        BaseKafkaDeserialization.super.open(context);
    }

    @Override
    public Map<String, Object> deserialize(ConsumerRecord<byte[], byte[]> record) throws Exception {
        byte[] value = record.value();
        Map<String, Object> map = JsonUtil.objectMapper().readValue(value, new TypeReference<Map<String, Object>>() {
        });
        if (attachMeta) {
            addInformation(map, record);
        }
        return map;
    }

    private void addInformation(Map<String, Object> map, ConsumerRecord<byte[], byte[]> record) {
        map.put("kafka.consumer.timestamp", record.timestamp());
        map.put("kafka.consumer.topic", record.topic());
        map.put("kafka.consumer.offset", record.offset());
        map.put("kafka.consumer.partition", record.partition());
    }

    @Override
    public boolean isEndOfStream(Map<String, Object> stringObjectMap) {
        return false;
    }

    @Override
    public TypeInformation<Map<String, Object>> getProducedType() {
        return TypeInformation.of(new TypeHint<Map<String, Object>>() {
        });
    }
}
