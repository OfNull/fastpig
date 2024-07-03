package com.ofnull.fastpig.blocks.info;

import com.ofnull.fastpig.blocks.RelationEnum;

/**
 * @author ofnull
 * @date 2022/2/16 15:02
 */
public class ModelRelationDo {
    private Long id;
    private Long modelId;
    private RelationEnum relation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public RelationEnum getRelation() {
        return relation;
    }

    public void setRelation(RelationEnum relation) {
        this.relation = relation;
    }
}
