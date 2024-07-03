package com.ofnull.fastpig.common.job;

import java.io.Serializable;

/**
 * @author ofnull
 * @date 2022/2/11 15:20
 */
public class JobInfo implements Serializable {
    private String name;
    private Integer parallelism;
    private Integer maxParallelism;
    private CheckpointInfo checkpoint;
    private RestartStrategyInfo restart;
    private boolean enableLocalWeb = false;
    private Integer webPort = 8082;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getParallelism() {
        return parallelism;
    }

    public void setParallelism(Integer parallelism) {
        this.parallelism = parallelism;
    }

    public Integer getMaxParallelism() {
        return maxParallelism;
    }

    public void setMaxParallelism(Integer maxParallelism) {
        this.maxParallelism = maxParallelism;
    }

    public CheckpointInfo getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(CheckpointInfo checkpoint) {
        this.checkpoint = checkpoint;
    }

    public RestartStrategyInfo getRestart() {
        return restart;
    }

    public void setRestart(RestartStrategyInfo restart) {
        this.restart = restart;
    }

    public boolean isEnableLocalWeb() {
        return enableLocalWeb;
    }

    public void setEnableLocalWeb(boolean enableLocalWeb) {
        this.enableLocalWeb = enableLocalWeb;
    }

    public Integer getWebPort() {
        return webPort;
    }

    public void setWebPort(Integer webPort) {
        this.webPort = webPort;
    }
}
