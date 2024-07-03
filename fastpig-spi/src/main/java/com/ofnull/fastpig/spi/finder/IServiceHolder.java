package com.ofnull.fastpig.spi.finder;

/**
 * 服务持有类
 *
 * @author ofnull
 * @date 2024/6/6 10:25
 */
public interface IServiceHolder<T> {
    T findService(String name, Class<T> type) throws Exception;

    T createNewService(String name, Class<T> type) throws Exception;
}
