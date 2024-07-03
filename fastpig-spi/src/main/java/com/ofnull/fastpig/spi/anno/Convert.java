package com.ofnull.fastpig.spi.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * BeanConvertMapper 转换注解
 *
 * @author ofnull
 * @date 2024/6/20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Convert {

    String type();
}
