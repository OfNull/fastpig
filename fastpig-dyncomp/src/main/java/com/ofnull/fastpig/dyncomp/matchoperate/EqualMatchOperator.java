package com.ofnull.fastpig.dyncomp.matchoperate;

import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.matchoperate.IMatchOperator;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ofnull
 * @date 2024/6/21
 */
@PigType("Equal")
public class EqualMatchOperator implements IMatchOperator {
    @Override
    public boolean eval(Object values, Object expectValues) {
        return StringUtils.equals(String.valueOf(values), String.valueOf(expectValues));
    }
}
