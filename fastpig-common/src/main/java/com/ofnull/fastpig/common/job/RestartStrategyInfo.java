package com.ofnull.fastpig.common.job;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;

import java.util.concurrent.TimeUnit;

/**
 * @author ofnull
 * @date 2022/2/11 15:23
 */
public class RestartStrategyInfo {
    private String strategy = "none";
    /**
     * fixed-delay  次数
     */
    private Integer attempts;
    /**
     * fixed-delay、failure-rate  单位秒
     */
    private Long delay;
    /**
     * failure-rate 间隔
     */
    private Integer maxFailuresPerInterval;
    /**
     * failure-rate 失败间隔 单位秒
     */
    private Integer failureRateInterval;

    private TimeUnit seconds = TimeUnit.SECONDS;

    private RestartStrategies.RestartStrategyConfiguration restartStrategyConfiguration;


    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public Integer getMaxFailuresPerInterval() {
        return maxFailuresPerInterval;
    }

    public void setMaxFailuresPerInterval(Integer maxFailuresPerInterval) {
        this.maxFailuresPerInterval = maxFailuresPerInterval;
    }

    public Integer getFailureRateInterval() {
        return failureRateInterval;
    }

    public void setFailureRateInterval(Integer failureRateInterval) {
        this.failureRateInterval = failureRateInterval;
    }

    public void check() throws RuntimeException {
        switch (strategy) {
            case "fixed-delay":
                if (attempts == null) {
                    throw new IllegalArgumentException("Start App Exception, Job Config 'RestartStrategy':'" + strategy + "' 'attempts' Is Null!");
                }
                if (attempts < 1) {
                    throw new IllegalArgumentException("Start App Exception, Job Config 'RestartStrategy':'" + strategy + "' 'attempts' < 1!");
                }
                if (delay == null) {
                    throw new IllegalArgumentException("Start App Exception, Job Config 'RestartStrategy':'" + strategy + "' 'delay' Is Null!");
                }
                if (delay < 1) {
                    throw new IllegalArgumentException("Start App Exception, Job Config 'RestartStrategy':'" + strategy + "' 'delay' < 1!");
                }
                break;
            case "failure-rate":
                if (delay == null) {
                    throw new IllegalArgumentException("Start App Exception, Job Config 'RestartStrategy':'" + strategy + "' 'delay' Is Null!");
                }
                if (delay < 1) {
                    throw new IllegalArgumentException("Start App Exception, Job Config 'RestartStrategy':'" + strategy + "' 'delay' < 1!");
                }
                if (maxFailuresPerInterval == null) {
                    throw new IllegalArgumentException("Start App Exception, Job Config 'RestartStrategy':'" + strategy + "' 'maxFailuresPerInterval' Is Null!");
                }
                if (maxFailuresPerInterval < 1) {
                    throw new IllegalArgumentException("Start App Exception, Job Config 'RestartStrategy':'" + strategy + "' 'maxFailuresPerInterval' < 1!");
                }
                if (failureRateInterval == null) {
                    throw new IllegalArgumentException("Start App Exception, Job Config 'RestartStrategy':'" + strategy + "' 'failureRateInterval' Is Null!");
                }
                if (failureRateInterval < 1) {
                    throw new IllegalArgumentException("Start App Exception, Job Config 'RestartStrategy':'" + strategy + "' 'failureRateInterval' < 1!");
                }
                break;
            case "none":
            default:
                break;
        }
    }

    public RestartStrategies.RestartStrategyConfiguration getRestartStrategyConfiguration() {
        switch (strategy) {
            case "fixed-delay":
                return RestartStrategies.fixedDelayRestart(
                        attempts, // 尝试重启的次数
                        Time.of(delay, seconds) // 延时
                );
            case "failure-rate":
                return RestartStrategies.failureRateRestart(
                        maxFailuresPerInterval, // 每个时间间隔的最大故障次数
                        Time.of(failureRateInterval, TimeUnit.MINUTES), // 测量故障率的时间间隔
                        Time.of(delay, TimeUnit.SECONDS) // 延时
                );
            case "none":
            default:
                return RestartStrategies.noRestart();

        }

    }
}
