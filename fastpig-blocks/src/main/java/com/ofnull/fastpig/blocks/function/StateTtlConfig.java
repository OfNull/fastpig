package com.ofnull.fastpig.blocks.function;

import java.io.Serializable;

/**
 * @author ofnull
 * @date 2022/2/21 15:43
 */
public class StateTtlConfig implements Serializable {
    //Unit SECONDS
    private Long invalidTime;
    private Boolean isReturnExpired = false;
    private Boolean isUpdateTtlByRead = false;
    private String clean = "FullSnapshot";
}
