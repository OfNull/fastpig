package com.ofnull.fastpig.blocks.info;

import java.io.Serializable;

/**
 * @author ofnull
 * @date 2022/2/16 14:33
 */
public class TaskDo implements Serializable {

    private Long id;

    private String name;

    private String describe;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }
}
