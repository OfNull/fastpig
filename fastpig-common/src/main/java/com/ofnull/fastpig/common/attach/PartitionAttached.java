package com.ofnull.fastpig.common.attach;

import java.io.Serializable;

/**
 * @author ofnull
 * @date 2024/6/18
 */
public class PartitionAttached implements Serializable {
    private Integer partition;
    private Integer parallelism;
    private String partitionKeys;

    public PartitionAttached() {
    }

    public PartitionAttached(Integer partition, Integer parallelism, String partitionKeys) {
        this.partition = partition;
        this.parallelism = parallelism;
        this.partitionKeys = partitionKeys;
    }

    public Integer getPartition() {
        return partition;
    }

    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public Integer getParallelism() {
        return parallelism;
    }

    public void setParallelism(Integer parallelism) {
        this.parallelism = parallelism;
    }

    public String getPartitionKeys() {
        return partitionKeys;
    }

    public void setPartitionKeys(String partitionKeys) {
        this.partitionKeys = partitionKeys;
    }
}
