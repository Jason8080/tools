package cn.gmlee.tools.swagger.assist;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.swagger.config.SwaggerGlobalProperties;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * springdoc-openapi 辅助（原 Springfox 已移除）.
 *
 * @author Jas °
 */
public class SwaggerAssist {
    /**
     * The constant separator.
     */
    protected static String separator = "/";

    /**
     * 添加静态资源映射（swagger-ui 由 springdoc 自动提供，此处仅保留业务 classpath:/static）
     *
     * @param registry the registry
     */
    public static void addResourceHandler(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * 解析自定义前缀.
     *
     * @param prefix the prefix
     * @return the string
     */
    public static String parserPrefix(String prefix) {
        if (!StringUtils.isEmpty(prefix) && !prefix.endsWith(separator)) {
            return prefix + separator;
        }
        return prefix;
    }

    /**
     * 全局 Header/Query 等参数（对应原 Springfox globalOperationParameters）.
     */
    public static OperationCustomizer globalOperationCustomizer(SwaggerGlobalProperties global) {
        return (operation, handlerMethod) -> {
            if (global == null || !Boolean.TRUE.equals(global.enable)) {
                return operation;
            }
            List<SwaggerGlobalProperties.GlobalParameter> list = global.getParameters();
            if (list == null || list.isEmpty()) {
                return operation;
            }
            String[] split = global.parametersIndex.split(",");
            boolean all = BoolUtil.isEmpty(split) || split.length == 0;
            List<String> index = Arrays.asList(split);
            for (int i = 0; i < list.size(); i++) {
                if (all || index.contains(String.valueOf(i))) {
                    SwaggerGlobalProperties.GlobalParameter p = list.get(i);
                    if (Boolean.TRUE.equals(p.getHidden())) {
                        continue;
                    }
                    Parameter param = new Parameter()
                            .in(p.getParamType())
                            .name(p.getName())
                            .description(p.getDescription())
                            .required(Boolean.TRUE.equals(p.getRequired()))
                            .schema(new StringSchema()._default(p.getDefaultValue()));
                    operation.addParametersItem(param);
                }
            }
            return operation;
        };
    }

    /**
     * 忽略指定类型的方法参数（对应原 Docket ignoredParameterTypes）.
     */
    public static OperationCustomizer ignoredParameterTypesCustomizer(String ignoredParameterTypes) {
        return (operation, handlerMethod) -> {
            if (handlerMethod == null || BoolUtil.isEmpty(ignoredParameterTypes)) {
                return operation;
            }
            Set<Class<?>> ignored = loadClasses(ignoredParameterTypes.split(","));
            if (ignored.isEmpty() || operation.getParameters() == null) {
                return operation;
            }
            for (org.springframework.core.MethodParameter mp : handlerMethod.getMethodParameters()) {
                if (ignored.contains(mp.getParameterType())) {
                    String name = mp.getParameterName();
                    if (name != null) {
                        operation.getParameters().removeIf(p -> name.equals(p.getName()));
                    }
                }
            }
            return operation;
        };
    }

    private static Set<Class<?>> loadClasses(String[] fqns) {
        Set<Class<?>> set = new HashSet<>();
        for (String fqn : fqns) {
            if (BoolUtil.notEmpty(fqn)) {
                try {
                    set.add(Class.forName(fqn.trim()));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return set;
    }
}
