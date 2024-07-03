package com.ofnull.fastpig.connector.kafka.ratelimit;

import com.ofnull.fastpig.connector.kafka.config.KafkaSourceConfig.RateLimitConfig;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.shaded.guava30.com.google.common.collect.HashBasedTable;
import org.apache.flink.shaded.guava30.com.google.common.collect.Table;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author ofnull
 * @date 2024/4/24 17:08
 */
public class SinglePartitionRateLimiter implements PartitionRateLimiter {
    public static final Logger logger = LoggerFactory.getLogger(SinglePartitionRateLimiter.class);

    private String limitTopic;
    private Long totalRateLimit;
    private Long totalRateUsed;
    private Long partitionRateLimit;
    private Long nsToSleep;

    private RateLimitConfig rateLimitConfig;


    private Table<String, Integer, Long> partitionState = HashBasedTable.create();

    public SinglePartitionRateLimiter(String limitTopic, RateLimitConfig rateLimitConfig) {
        this.limitTopic = limitTopic;
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public void open(RuntimeContext runtimeContext) throws Exception {
        //初始化限流信息
        this.totalRateLimit = rateLimitConfig.getReteLimit();
        this.totalRateUsed = 0L; //可以实现差速限流 本类暂不实现 思路可以从其他主题统计窗口内的消费数量 然后通过消息或者暴露回调方法 填充该字段并重新计算速率
        resetRate(); // 计算速率
    }

    protected void resetRate() throws ExecutionException, InterruptedException {
        int partitionNumber = getLimitTopicPartitions();
        //计算单分区限流数
        this.partitionRateLimit = Math.max((totalRateLimit - totalRateUsed), 0) / partitionNumber;
        //求平均处理每条记录需要的纳秒时间
        if (partitionRateLimit == 0) {
            this.nsToSleep = rateLimitConfig.getTimeUnit().toNanos(rateLimitConfig.getTimeDuration());
        } else {
            this.nsToSleep = rateLimitConfig.getTimeUnit().toNanos(rateLimitConfig.getTimeDuration()) / partitionRateLimit;
        }
    }

    protected int getLimitTopicPartitions() throws ExecutionException, InterruptedException {
        AdminClient adminClient = AdminClient.create(rateLimitConfig.getProperties());
        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(Arrays.asList(limitTopic));
        Map<String, TopicDescription> topicsResult = describeTopicsResult.all().get();
        TopicDescription topicDescription = topicsResult.get(limitTopic);
        adminClient.close();
        return topicDescription.partitions().size();
    }

    // 获取许可数量
    @Override
    public PermitAcquired acquire(String topic, int partition, long permits) {
        Long lastTime = partitionState.get(topic, partition);
        long currentNanoTime = System.nanoTime();
        if (lastTime == null) {
            //首次进来 更新分区状态 返回许可请求对象
            partitionState.put(topic, partition, currentNanoTime);
            return new PermitAcquired(permits, 0);
        }
        partitionState.put(topic, partition, currentNanoTime);
        //通过（当前时间 - 上次获取许可时间 ）/ 每条记录需要的纳秒时间 得到可以获取的有效许可数量
        long permitsAvailable = (currentNanoTime - lastTime) / nsToSleep;
        //为了避免 有效许可数量 大于 实际请求许可数量 这里取个最小值
        permitsAvailable = Math.min(permitsAvailable, permits);
        //获取下次可以获取许可的等待间隔时间
        long waitNano = (permits - permitsAvailable) * nsToSleep;
        //防止等待时间超过系统配置的等待时间 取最小值
        waitNano = Math.min(waitNano, rateLimitConfig.getTimeUnit().toNanos(rateLimitConfig.getTimeDuration()));
        return new PermitAcquired(permitsAvailable, waitNano);
    }

    @Override
    public Long getTotalRateLimit() {
        return this.totalRateLimit;
    }

    @Override
    public Long getTotalRateUsed() {
        return this.totalRateUsed;
    }

    @Override
    public Long getPartitionRateLimit() {
        return this.partitionRateLimit;
    }
    @Override
    public void setRateLimitConfig(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public void close() {

    }

}
