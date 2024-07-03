package com.ofnull.fastpig.spi.job;

/**
 * @author ofnull
 * @date 2024/6/17 14:55
 */
public interface IJobConfiguration {

    <T> T convertConfigToEntity(String key, Class<T> clazz) throws Exception;

    <T> T getJobInfo() throws Exception;

}
