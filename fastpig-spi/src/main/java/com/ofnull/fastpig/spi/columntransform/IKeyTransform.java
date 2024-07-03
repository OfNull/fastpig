package com.ofnull.fastpig.spi.columntransform;

/**
 * @author ofnull
 * @date 2024/6/12 19:14
 */
public interface IKeyTransform {
    Object beforeWrite(Object value);

    Object afterRead(Object value);
}
