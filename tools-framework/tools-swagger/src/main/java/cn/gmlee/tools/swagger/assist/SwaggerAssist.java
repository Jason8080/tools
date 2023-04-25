package cn.gmlee.tools.swagger.assist;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.swagger.config.SwaggerGlobalProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The type Swagger assist.
 *
 * @author Jas °
 * @date 2020 /11/4 (周三)
 */
public class SwaggerAssist {
    /**
     * The constant separator.
     */
    protected static String separator = "/";

    /**
     * 添加静态资源映射
     *
     * @param registry the registry
     */
    public static void addResourceHandler(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
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
     * Gets global operation parameters.
     *
     * @param global the enable
     * @return the global operation parameters
     */
    public static List<Parameter> getGlobalOperationParameters(SwaggerGlobalProperties global) {
        if (global.enable) {
            List<Parameter> parameters = new ArrayList();
            List<SwaggerGlobalProperties.GlobalParameter> list = global.getParameters();
            String[] split = global.parametersIndex.split(",");
            boolean all = BoolUtil.isEmpty(parameters);
            List<String> index = Arrays.asList(split);
            for (Integer i = 0; i < list.size(); i++) {
                if (all || index.contains(i.toString())) {
                    SwaggerGlobalProperties.GlobalParameter parameter = list.get(i);
                    ParameterBuilder parameterBuilder = new ParameterBuilder()
                            .name(parameter.getName())
                            .description(parameter.getDescription())
                            .defaultValue(parameter.getDefaultValue())
                            .required(parameter.getRequired())
                            .modelRef(new ModelRef(parameter.getJavaType()))
                            .parameterType(parameter.getParamType())
                            .hidden(parameter.getHidden())
                            .order(i);
                    parameters.add(parameterBuilder.build());
                }
            }
            return parameters;
        }
        return Collections.emptyList();
    }
}
