package com.ofnull.fastpig.dyncomp.matchoperate;

import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.matchoperate.IMatchOperator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.Collection;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author ofnull
 * @date 2024/6/21
 */
@PigType("NotEmpty")
public class NotEmptyMatchOperator implements IMatchOperator {
    @Override
    public boolean eval(Object values, Object expectValues) {
        if (values == null) {
            return false;
        }
        if (values instanceof String) {
            return isNotEmpty(String.valueOf(values));
        }
        if (values instanceof Collection) {
            return CollectionUtils.isNotEmpty((Collection<?>) values);
        }
        if (values instanceof Map) {
            return MapUtils.isNotEmpty((Map<?, ?>) values);
        }
        return true;
    }
}
