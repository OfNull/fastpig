package com.ofnull.fastpig.blocks.column.transform;

import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.columntransform.IKeyTransform;
import org.apache.commons.lang3.StringUtils;

/**
 * 反转字符串
 *
 * @author ofnull
 * @date 2024/6/18
 */
@PigType("Reverse")
public class ReverseKeyTransform implements IKeyTransform {
    @Override
    public Object beforeWrite(Object value) {
        if (value == null) return null;
        return StringUtils.reverse(String.valueOf(value));
    }

    @Override
    public Object afterRead(Object value) {
        if (value == null) return null;
        return StringUtils.reverse(String.valueOf(value));
    }
}
