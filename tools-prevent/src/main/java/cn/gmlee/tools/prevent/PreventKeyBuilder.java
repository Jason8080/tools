package cn.gmlee.tools.prevent;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 防刷Key生成器
 *
 * @author James
 * @since 2023-07-26
 */
public class PreventKeyBuilder {

    private static final String PREVENT_KEY_PREFIX = "prevent";

    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 构建key 优先级别  自定义key--->请求URI--->方法全名(类全名+方法名)
     *
     * @param requestURI          请求URLI
     * @param invocation          MethodInvocation
     * @param definitionKey       缓存key
     * @param definitionKeyParams key参数
     * @return 实际缓存key
     */
    public static String buildKey(String requestURI, MethodInvocation invocation, String definitionKey, String[] definitionKeyParams) {
        StringBuilder sb = new StringBuilder(getKeyPrefix());
        Method method = invocation.getMethod();
        sb.append(":");
        sb.append(!StringUtils.isEmpty(definitionKey) ? definitionKey : !StringUtils.isEmpty(requestURI) ? requestURI : method.getDeclaringClass().getName() + "." + method.getName());
        if (definitionKeyParams.length > 1 || !"".equals(definitionKeyParams[0])) {
            sb.append(":").append(getSpelDefinitionKeyParam(definitionKeyParams, method, invocation.getArguments()));
        }
        return sb.toString();
    }

    static protected String getSpelDefinitionKeyParam(String[] definitionKeyParams, Method method, Object[] parameterValues) {
        EvaluationContext context = new MethodBasedEvaluationContext(null, method, parameterValues, NAME_DISCOVERER);
        List<String> definitionKeyParamList = new ArrayList<>(definitionKeyParams.length);
        for (String definitionKeyParam : definitionKeyParams) {
            if (definitionKeyParam != null && !definitionKeyParam.isEmpty()) {
                String keyParam = PARSER.parseExpression(definitionKeyParam).getValue(context).toString();
                definitionKeyParamList.add(keyParam);
            }
        }
        return StringUtils.collectionToDelimitedString(definitionKeyParamList, ".", "", "");
    }

    static protected String getKeyPrefix() {
        return PREVENT_KEY_PREFIX;
    }

}
