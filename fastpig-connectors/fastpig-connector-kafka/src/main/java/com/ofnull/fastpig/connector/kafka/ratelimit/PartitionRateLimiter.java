package com.ofnull.fastpig.connector.kafka.ratelimit;

import com.ofnull.fastpig.connector.kafka.config.KafkaSourceConfig.RateLimitConfig;
import org.apache.flink.api.common.functions.RuntimeContext;

import java.io.Serializable;

/**
 * @author ofnull
 * @date 2024/4/24 16:57
 */
public interface PartitionRateLimiter extends Serializable {

    void open(RuntimeContext runtimeContext) throws Exception;

    PermitAcquired acquire(String topic, int partition, long permits);

    //total rate limit
    Long getTotalRateLimit();

    //use rate limit
    Long getTotalRateUsed();

    // single partition rate limit
    Long getPartitionRateLimit();

    void setRateLimitConfig(RateLimitConfig rateLimitConfig);

    void close();

    static class PermitAcquired {
        private long acquired;

        private long waitNano;

        public PermitAcquired(long acquired, long waitNano) {
            this.acquired = acquired;
            this.waitNano = waitNano;
        }

        public long getAcquired() {
            return acquired;
        }

        public void setAcquired(long acquired) {
            this.acquired = acquired;
        }

        public long getWaitNano() {
            return waitNano;
        }

        public void setWaitNano(long waitNano) {
            this.waitNano = waitNano;
        }
    }
}
