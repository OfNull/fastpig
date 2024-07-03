package com.ofnull.fastpig.connector.kafka.deserialize;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @author ofnull
 * @date 2022/2/14 15:52
 */
public class ConsumerDeserializationSchema<T> implements KafkaDeserializationSchema<T> {
    private ObjectMapper om = new ObjectMapper();

    @Override
    public boolean isEndOfStream(T nextElement) {
        return false;
    }

    @Override
    public T deserialize(ConsumerRecord<byte[], byte[]> record) throws Exception {
        T data = om.readValue(record.value(), new TypeReference<T>() {
        });
        return data;
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return TypeInformation.of(new TypeHint<T>() {
        });
    }
}
