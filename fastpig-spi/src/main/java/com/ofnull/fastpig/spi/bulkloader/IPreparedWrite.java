package com.ofnull.fastpig.spi.bulkloader;

/**
 * @author ofnull
 * @date 2024/6/12 11:32
 */
public interface IPreparedWrite {

    boolean isNotEmpty();

    <T> T getData();
}
