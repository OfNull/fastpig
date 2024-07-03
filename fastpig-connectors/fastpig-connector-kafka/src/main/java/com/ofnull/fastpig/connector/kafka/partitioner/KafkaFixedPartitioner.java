package com.ofnull.fastpig.connector.kafka.partitioner;

import com.ofnull.fastpig.spi.connector.kafka.BaseKafkaPartitioner;
import org.apache.flink.util.Preconditions;

/**
 * from org.apache.flink.streaming.connectors.kafka.partitioner.FlinkFixedPartitioner
 *
 * @author ofnull
 * @date 2024/6/6 14:50
 */
public class KafkaFixedPartitioner<T> extends BaseKafkaPartitioner<T> {
    private int parallelInstanceId;

    @Override
    public void open(int parallelInstanceId, int parallelInstances) {
        Preconditions.checkArgument(
                parallelInstanceId >= 0, "Id of this subtask cannot be negative.");
        Preconditions.checkArgument(
                parallelInstances > 0, "Number of subtasks must be larger than 0.");

        this.parallelInstanceId = parallelInstanceId;
    }

    @Override
    public int partition(T record, byte[] key, byte[] value, String targetTopic, int[] partitions) {
        Preconditions.checkArgument(
                partitions != null && partitions.length > 0,
                "Partitions of the target topic is empty.");

        return partitions[parallelInstanceId % partitions.length];
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof KafkaFixedPartitioner;
    }

    @Override
    public int hashCode() {
        return KafkaFixedPartitioner.class.hashCode();
    }
}
