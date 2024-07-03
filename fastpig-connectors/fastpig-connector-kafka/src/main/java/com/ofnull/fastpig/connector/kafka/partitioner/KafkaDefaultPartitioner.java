package com.ofnull.fastpig.connector.kafka.partitioner;

import com.ofnull.fastpig.spi.connector.kafka.BaseKafkaPartitioner;
import org.apache.flink.shaded.guava30.com.google.common.hash.Hashing;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ofnull
 * @date 2024/6/6 14:54
 */
public class KafkaDefaultPartitioner<T> extends BaseKafkaPartitioner<T> {
    private AtomicLong counter = new AtomicLong(0);

    @Override
    public int partition(T record, byte[] key, byte[] value, String targetTopic, int[] partitions) {
        if (key == null) {
            int index = (int) Math.abs(counter.incrementAndGet() % partitions.length);
            return partitions[index];
        }
        long hash = Math.abs(Hashing.murmur3_128().newHasher().putBytes(key).hash().asLong());
        if (hash < 0) {
            hash = 0;
        }
        return partitions[(int) (hash % partitions.length)];
    }


}
