package com.ofnull.fastpig.common.job;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;

import static org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup.*;

/**
 * @author ofnull
 * @date 2022/2/11 15:22
 */
public class CheckpointInfo {
    private Boolean enable = true;

    private Long betweenInterval = 1000L;
    private Long interval = 1000L;
    private Long timeout;
    private CheckpointConfig.ExternalizedCheckpointCleanup cleanup;
    private Integer concurrent = 1;
    private CheckpointingMode checkpointingMode = CheckpointingMode.EXACTLY_ONCE;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Long getBetweenInterval() {
        return betweenInterval;
    }

    public void setBetweenInterval(Long betweenInterval) {
        this.betweenInterval = betweenInterval;
    }

    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public CheckpointConfig.ExternalizedCheckpointCleanup getCleanup() {
        return cleanup;
    }

    public void setCleanup(String cleanup) {
        if (StringUtils.isBlank(cleanup)) {
            return;
        }
        switch (cleanup) {
            case "RETAIN_ON_CANCELLATION":
                this.cleanup = RETAIN_ON_CANCELLATION;
                break;
            case "DELETE_ON_CANCELLATION":
                this.cleanup = DELETE_ON_CANCELLATION;
                break;
            case "NO_EXTERNALIZED_CHECKPOINTS":
                this.cleanup = NO_EXTERNALIZED_CHECKPOINTS;
                break;
            default:
                return;
        }
    }

    public Integer getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(Integer concurrent) {
        this.concurrent = concurrent;
    }

    public CheckpointingMode getCheckpointingMode() {
        return checkpointingMode;
    }

    public void setCheckpointingMode(CheckpointingMode checkpointingMode) {
        this.checkpointingMode = checkpointingMode;
    }
}
