package com.ofnull.fastpig.spi.metainfo;

import com.ofnull.fastpig.spi.anno.Convert;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author ofnull
 * @date 2024/6/20
 */
public class JobRecordProcessMetaInfo implements Serializable {
    private Long id;
    @Convert(type = "json")
    private MatchRules matchRules;
    private String yesProcessor;
    private String yesConfig;
    private String noProcessor;
    private String noConfig;

    @Convert(type = "json")
    private FailPolicy failPolicy;

    private String subFlow;
    private Integer orders;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MatchRules getMatchRules() {
        return matchRules;
    }

    public void setMatchRules(MatchRules matchRules) {
        this.matchRules = matchRules;
    }

    public String getYesProcessor() {
        return yesProcessor;
    }

    public void setYesProcessor(String yesProcessor) {
        this.yesProcessor = yesProcessor;
    }

    public String getYesConfig() {
        return yesConfig;
    }

    public void setYesConfig(String yesConfig) {
        this.yesConfig = yesConfig;
    }

    public String getNoProcessor() {
        return noProcessor;
    }

    public void setNoProcessor(String noProcessor) {
        this.noProcessor = noProcessor;
    }

    public String getNoConfig() {
        return noConfig;
    }

    public void setNoConfig(String noConfig) {
        this.noConfig = noConfig;
    }

    public FailPolicy getFailPolicy() {
        return failPolicy;
    }

    public void setFailPolicy(FailPolicy failPolicy) {
        this.failPolicy = failPolicy;
    }

    public String getSubFlow() {
        return subFlow;
    }

    public void setSubFlow(String subFlow) {
        this.subFlow = subFlow;
    }

    public Integer getOrders() {
        return orders;
    }

    public void setOrders(Integer orders) {
        this.orders = orders;
    }
}
