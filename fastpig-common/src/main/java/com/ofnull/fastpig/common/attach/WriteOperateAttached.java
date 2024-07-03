package com.ofnull.fastpig.common.attach;

import com.ofnull.fastpig.spi.bulkloader.WriteOperateEnum;

import java.io.Serializable;

/**
 * 数据写入级别操作
 *
 * @author ofnull
 * @date 2024/6/17
 */
public class WriteOperateAttached implements Serializable {
    private WriteOperateEnum operateEnum;

    private WriteOperateAttached() {
    }

    public static WriteOperateAttached of(WriteOperateEnum operateEnum) {
        return new WriteOperateAttached(operateEnum);
    }

    private WriteOperateAttached(WriteOperateEnum operateEnum) {
        this.operateEnum = operateEnum;
    }


    public WriteOperateEnum getOperateEnum() {
        return operateEnum;
    }

    public void setOperateEnum(WriteOperateEnum operateEnum) {
        this.operateEnum = operateEnum;
    }
}
