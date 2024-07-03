package com.ofnull.fastpig.common.utils;

import com.ofnull.fastpig.common.finder.LoadedServices;
import com.ofnull.fastpig.spi.matchoperate.IMatchOperator;
import com.ofnull.fastpig.spi.metainfo.MatchRule;
import com.ofnull.fastpig.spi.metainfo.MatchRules;
import com.ofnull.fastpig.spi.recordprocessor.IJsonPathSupportedMap;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.MapUtils.isEmpty;

/**
 * 规则匹配判断器
 *
 * @author ofnull
 * @date 2024/6/24
 */
public class MatchRuleUtils {
    public static boolean matches(LoadedServices<IMatchOperator> matchOperators, Map<String, Object> event, MatchRules rules) throws Exception {
        if (isEmpty(rules)) {
            return true;
        }
        IJsonPathSupportedMap jpsMap = new JsonPathSupportedMap(event);
        return matches(matchOperators, jpsMap, rules);
    }

    public static boolean matches(LoadedServices<IMatchOperator> matchOperators, IJsonPathSupportedMap event, MatchRules rules) throws Exception {
        if (isEmpty(rules)) {
            return true;
        }
        for (Map.Entry<String, List<MatchRule>> entry : rules.entrySet()) {
            List<MatchRule> rule = entry.getValue();
            if (matches(matchOperators, event, rule)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(LoadedServices<IMatchOperator> matchOperators, IJsonPathSupportedMap event, List<MatchRule> rules) throws Exception {
        if (CollectionUtils.isEmpty(rules))
            return true;
        for (MatchRule rule : rules) {
            String key = rule.getKey();
            Object value = event.get(key);
            IMatchOperator matchOperator = matchOperators.findService(rule.getOperator(), IMatchOperator.class);
            if (matchOperator.eval(value, rule.getValues()) == false) {
                return false;
            }
        }
        return true;
    }
}
