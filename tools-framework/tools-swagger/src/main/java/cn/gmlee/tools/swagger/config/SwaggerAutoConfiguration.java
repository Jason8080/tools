package cn.gmlee.tools.swagger.config;

import cn.gmlee.tools.base.util.IocUtil;
import cn.gmlee.tools.swagger.assist.SwaggerAssist;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.AnnotationUtils;
import jakarta.annotation.Resource;

import java.lang.reflect.Method;

/**
 * springdoc-openapi 通用配置（替代 Springfox）.
 * <p>
 *     matchIfMissing: true -> 丢失该配置可以注入
 *     havingValue: "xxx" -> 配置必须与xxx相同方可注入
 *     默认值: tools.webapp.swagger.close=false
 *     总结开启方式: 不配置close, 或者将close配置成false
 * </p>
 *
 * @author Jas °
 */
@Configuration
@EnableConfigurationProperties(SwaggerGlobalProperties.class)
@ConditionalOnProperty(prefix = "tools.webapp.swagger", value = "close", matchIfMissing = true, havingValue = "false")
@PropertySource(value = {"classpath:swagger.properties", "classpath:application.properties", "classpath:application-${spring.profiles.active}.properties"}, ignoreResourceNotFound = true)
public class SwaggerAutoConfiguration {
    @Value("${tools.webapp.swagger.prefix:}")
    protected String prefix;
    @Value("${tools.webapp.swagger.title:开放平台文档在线系统}")
    protected String title;
    @Value("${tools.webapp.swagger.groups:默认文档:/**}")
    protected String groups;
    @Value("${tools.webapp.swagger.ignoredParameterTypes:}")
    protected String ignoredParameterTypes;

    @Resource
    private SwaggerGlobalProperties swaggerGlobalProperties;

    @Bean
    public OpenAPI toolsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description("OpenAPI 通用接口文档")
                        .version("1.0.0"));
    }

    @Bean
    public OperationCustomizer toolsSwaggerGlobalOperationCustomizer() {
        return SwaggerAssist.globalOperationCustomizer(swaggerGlobalProperties);
    }

    @Bean
    public OperationCustomizer toolsSwaggerIgnoredParameterTypesCustomizer() {
        return SwaggerAssist.ignoredParameterTypesCustomizer(ignoredParameterTypes);
    }

    /**
     * 注册分组文档；仅扫描带 {@link ApiOperation} 的方法（与旧 Springfox 行为一致）.
     */
    @Bean
    public GroupedOpenApi toolsSwaggerGroupedOpenApi(ApplicationContext applicationContext) {
        ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) applicationContext;
        String[] split = groups.split(",");
        for (int i = 1; i < split.length; i++) {
            String[] parts = split[i].split(":");
            if (parts.length >= 2) {
                GroupedOpenApi g = buildGroupedOpenApi(parts[0].trim(), parts[1].trim());
                IocUtil.registerBean(ctx, "swaggerGroupedOpenApi_" + i, g);
            }
        }
        String[] first = split[0].split(":");
        String groupName = first[0].trim();
        String pathPattern = first.length > 1 ? first[1].trim() : "/**";
        return buildGroupedOpenApi(groupName, pathPattern);
    }

    private static GroupedOpenApi buildGroupedOpenApi(String groupName, String pathPattern) {
        return GroupedOpenApi.builder()
                .group(groupName)
                .displayName(groupName)
                .pathsToMatch(pathPattern)
                .addOpenApiMethodFilter(SwaggerAutoConfiguration::hasApiOperation)
                .build();
    }

    private static boolean hasApiOperation(Method method) {
        return method != null && AnnotationUtils.findAnnotation(method, ApiOperation.class) != null;
    }
}
