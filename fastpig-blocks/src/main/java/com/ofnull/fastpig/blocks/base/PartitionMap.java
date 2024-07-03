package com.ofnull.fastpig.blocks.base;

import com.ofnull.fastpig.common.attach.PartitionAttached;
import com.ofnull.fastpig.common.attach.ServerProcessAttached;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.guava30.com.google.common.collect.Lists;
import org.apache.flink.shaded.guava30.com.google.common.hash.Hashing;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * @author ofnull
 * @date 2024/6/18
 */
public class PartitionMap extends RichFlatMapFunction<Map<String, Object>, Map<String, Object>> {
    private final List<String> partitionKeys;
    private final String joinedPartitionKeys;
    private int maxParallelism;

    public PartitionMap(List<String> partitionKeys) {
        this.partitionKeys = partitionKeys;
        this.joinedPartitionKeys = StringUtils.join(partitionKeys, ",");
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        this.maxParallelism = getRuntimeContext().getMaxNumberOfParallelSubtasks();
    }

    @Override
    public void flatMap(Map<String, Object> event, Collector<Map<String, Object>> collector) throws Exception {

        ServerProcessAttached processAttached = ServerProcessAttached.toServerProcessAttached(event);
        if (processAttached == null) {
            processAttached = new ServerProcessAttached();
        }
        if (processAttached.getPartitionAttached() == null) {
            int partition = getPartition(event, maxParallelism);
            PartitionAttached partitionAttached = new PartitionAttached(partition, maxParallelism, joinedPartitionKeys);
            processAttached.setPartitionAttached(partitionAttached);
        }
        event.put(ServerProcessAttached.SERVER_PROCESS_ATTACHED, processAttached.toMap());
        collector.collect(event);
    }

    private int getPartition(Map<String, Object> event, int parallel) throws Exception {
        List<Object> list = Lists.newArrayList();
        for (String field : partitionKeys) {
            list.add(event.get(field));
        }
        String key = StringUtils.join(list, ":");
        int hash = Hashing.murmur3_128().newHasher().putString(key, UTF_8).hash().asInt();
        return Math.abs(hash % parallel);
    }
}
