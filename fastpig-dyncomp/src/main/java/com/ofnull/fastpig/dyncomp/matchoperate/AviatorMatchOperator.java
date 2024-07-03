package com.ofnull.fastpig.dyncomp.matchoperate;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.lexer.token.OperatorType;
import com.ofnull.fastpig.common.utils.JsonUtil;
import com.ofnull.fastpig.spi.anno.PigType;
import com.ofnull.fastpig.spi.matchoperate.IMatchOperator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Aviator 脚本表达式
 * <p>
 * Example
 * 数据
 * {
 * "name": "xiaoming",
 * "age": 100,
 * "desc": "test"
 * }
 * 条件配置
 * {
 * "key": "name == 'xiaoming' and age == 100",
 * "operator": "Aviator",
 * "values" : "$."
 * }
 * 输出结果：true
 *
 * @author ofnull
 * @date 2024/6/25
 */
@PigType("Aviator")
public class AviatorMatchOperator implements IMatchOperator {
    private static final Logger LOG = LoggerFactory.getLogger(AviatorMatchOperator.class);
    private AviatorEvaluatorInstance instance = AviatorEvaluator.getInstance();
    private String globalCondition = null;
    private Expression expression;

    {
        instance.aliasOperator(OperatorType.AND, "and");
        instance.aliasOperator(OperatorType.OR, "or");
        instance.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
    }

    @Override
    public boolean eval(Object condition, Object expectValues) {

        if (globalCondition == null || !StringUtils.equals(String.valueOf(condition), globalCondition)) {
            AviatorEvaluator.validate((String) condition);
            globalCondition = String.valueOf(condition);
            expression = instance.compile("flink_" + globalCondition.hashCode(), this.globalCondition, true);
        }
        if (expectValues instanceof Map) {
            Boolean res = (Boolean) expression.execute((Map<String, Object>) expectValues);
            return res;
        }
        LOG.warn("Aviator expectValues Type nonMap, expression:{}, expect:{}", expression, JsonUtil.toJsonString(expectValues));
        return false;
    }
}
