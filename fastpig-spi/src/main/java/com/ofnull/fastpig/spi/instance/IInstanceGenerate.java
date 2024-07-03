package com.ofnull.fastpig.spi.instance;

import java.util.Map;

/**
 * @author ofnull
 * @date 2022/2/11 18:00
 */
public interface IInstanceGenerate<T> {


    void open(Map<String, Object> cfg);

    T generateInstance();

}
