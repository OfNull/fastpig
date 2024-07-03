package com.ofnull.fastpig.common.attach;

import java.io.Serializable;

/**
 * 数据行为附加信息
 *
 * @author ofnull
 * @date 2024/6/18
 */
public class ActionOperateAttached implements Serializable {

    private OperationType operationType;

    public ActionOperateAttached() {
    }

    public ActionOperateAttached(OperationType operationType) {
        this.operationType = operationType;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public enum OperationType {
        ADD, MERGE, REMOVE, INIT
    }
}
