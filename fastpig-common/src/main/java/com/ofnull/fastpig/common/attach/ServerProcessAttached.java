package com.ofnull.fastpig.common.attach;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ofnull.fastpig.common.utils.JsonUtil;

import java.io.Serializable;
import java.util.Map;

/**
 * @author ofnull
 * @date 2024/6/17
 */
public class ServerProcessAttached implements Serializable {
    public static final String SERVER_PROCESS_ATTACHED = "SERVER_PROCESS_ATTACHED";

    private ActionOperateAttached actionOperateAttached;

    private WriteOperateAttached writeOperateAttached;

    private PartitionAttached partitionAttached;

    public ActionOperateAttached getActionOperateAttached() {
        return actionOperateAttached;
    }

    public void setActionOperateAttached(ActionOperateAttached actionOperateAttached) {
        this.actionOperateAttached = actionOperateAttached;
    }

    public WriteOperateAttached getWriteOperateAttached() {
        return writeOperateAttached;
    }

    public void setWriteOperateAttached(WriteOperateAttached writeOperateAttached) {
        this.writeOperateAttached = writeOperateAttached;
    }

    public PartitionAttached getPartitionAttached() {
        return partitionAttached;
    }

    public void setPartitionAttached(PartitionAttached partitionAttached) {
        this.partitionAttached = partitionAttached;
    }

    public static ServerProcessAttached toServerProcessAttached(Map<String, Object> event) {
        return JsonUtil.convert(event.get(SERVER_PROCESS_ATTACHED), ServerProcessAttached.class);
    }

    public Map<String, Object> toMap() throws JsonProcessingException {
        return JsonUtil.objectMapper().readValue(JsonUtil.toJsonString(this), Map.class);
    }
}
