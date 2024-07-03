package com.ofnull.fastpig.blocks.function;

import java.io.Serializable;

/**
 * @author ofnull
 * @date 2022/2/21 15:26
 */
public class StateConfig implements Serializable {
    public static final String STATE = "state";
    private String name;
    private StateTtlConfig stateTtlConfig;
    private String dataType;

}
