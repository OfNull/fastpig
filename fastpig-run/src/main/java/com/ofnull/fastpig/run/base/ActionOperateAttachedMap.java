package com.ofnull.fastpig.run.base;

import com.ofnull.fastpig.common.attach.ActionOperateAttached;
import com.ofnull.fastpig.common.attach.ServerProcessAttached;
import com.ofnull.fastpig.common.utils.JsonUtil;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.Map;

/**
 * 添加动作操作符
 *
 * @author ofnull
 * @date 2024/6/18
 */
public class ActionOperateAttachedMap implements MapFunction<Map<String, Object>, Map<String, Object>> {
    private final ActionOperateAttached defaultActionOperate;

    public ActionOperateAttachedMap(ActionOperateAttached defaultActionOperate) {
        this.defaultActionOperate = defaultActionOperate;
    }

    @Override
    public Map<String, Object> map(Map<String, Object> event) throws Exception {
        ServerProcessAttached processAttached = JsonUtil.convert(event.get(ServerProcessAttached.SERVER_PROCESS_ATTACHED), ServerProcessAttached.class);
        if (processAttached == null) {
            processAttached = new ServerProcessAttached();
            event.put(ServerProcessAttached.SERVER_PROCESS_ATTACHED, processAttached);
        }
        if (processAttached.getActionOperateAttached() == null) {
            processAttached.setActionOperateAttached(defaultActionOperate);
        }
        return event;
    }
}
