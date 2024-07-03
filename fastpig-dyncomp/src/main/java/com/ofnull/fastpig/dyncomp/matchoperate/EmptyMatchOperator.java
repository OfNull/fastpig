package com.ofnull.fastpig.dyncomp.matchoperate;

import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.matchoperate.IMatchOperator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.Collection;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author ofnull
 * @date 2024/6/21
 */
@PigType("Empty")
public class EmptyMatchOperator implements IMatchOperator {
    @Override
    public boolean eval(Object values, Object expectValues) {
        if (values == null) {
            return true;
        }
        if (values instanceof String) {
            return isEmpty(String.valueOf(values));
        }
        if (values instanceof Collection) {
            return CollectionUtils.isEmpty((Collection<?>) values);
        }
        if (values instanceof Map) {
            return MapUtils.isEmpty((Map<?, ?>) values);
        }
        return false;
    }
}
