package com.ofnull.fastpig.spi.metainfo;

import java.io.Serializable;

/**
 * @author ofnull
 * @date 2024/6/14 16:49
 */
public interface IMetaLoader<R> extends Serializable {


    R loader() throws Exception;
}
