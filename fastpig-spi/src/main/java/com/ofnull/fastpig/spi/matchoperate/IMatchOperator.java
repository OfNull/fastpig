package com.ofnull.fastpig.spi.matchoperate;

import java.io.Closeable;

/**
 * @author ofnull
 * @date 2024/6/20
 */
public interface IMatchOperator extends Closeable {

    boolean eval(Object values, Object expectValues);

    @Override
    default void close(){}

}
