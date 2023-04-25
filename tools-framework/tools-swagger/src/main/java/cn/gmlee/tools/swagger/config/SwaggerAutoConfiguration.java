package cn.gmlee.tools.swagger.config;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.IocUtil;
import cn.gmlee.tools.swagger.assist.SwaggerAssist;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Swagger通用配置.
 * <p>
 *     matchIfMissing: true -> 丢失该配置可以注入
 *     havingValue: "xxx" -> 配置必须与xxx相同方可注入
 *     默认值: tools.webapp.swagger.close=false
 *     总结开启方式: 不配置close, 或者将close配置成false
 * </p>
 *
 * @author Jas °
 */
@EnableSwagger2
@EnableConfigurationProperties(SwaggerGlobalProperties.class)
@ConditionalOnProperty(prefix = "tools.webapp.swagger", value = "close", matchIfMissing = true, havingValue = "false")
@PropertySource(value = {"classpath:swagger.properties","classpath:application.properties","classpath:application-${spring.profiles.active}.properties"}, ignoreResourceNotFound = true)
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
    SwaggerGlobalProperties swaggerGlobalProperties;

    /**
     * 注册在线文档.
     *
     * @param applicationContext the application context
     * @return the docket
     */
    @Bean
    public Docket docket(ApplicationContext applicationContext) {
        try {
            return registerGroups((ConfigurableApplicationContext) applicationContext);
        } catch (Exception e) {
            e.printStackTrace();
            return new Docket(DocumentationType.SWAGGER_2)
                    .pathMapping(SwaggerAssist.parserPrefix(prefix))
                    .apiInfo(apiInfo())
                    .select()
                    .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                    .paths(PathSelectors.any())
                    .build()
                    .globalOperationParameters(SwaggerAssist.getGlobalOperationParameters(swaggerGlobalProperties))
                    .ignoredParameterTypes(getIgnoredParameterTypes(ignoredParameterTypes));
        }
    }

    private Docket registerGroups(ConfigurableApplicationContext applicationContext) {
        String[] split = groups.split(",");
        for (int i = 1; i < split.length; i++) {
            Docket docket = IocUtil.registerBean(
                    applicationContext,
                    String.format("Docket%s", i),
                    Docket.class,
                    DocumentationType.SWAGGER_2
            );
            String[] groups = split[i].split(":");
            registerDocket(docket, groups[0], groups[1]);
        }
        String[] groups = split[0].split(":");
        return registerDocket(new Docket(DocumentationType.SWAGGER_2), groups[0], groups[1]);
    }

    private Docket registerDocket(Docket docket, String groupName, String pathPattern) {
        return docket.pathMapping(SwaggerAssist.parserPrefix(prefix))
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.ant(pathPattern))
                .build()
                .groupName(groupName)
                .globalOperationParameters(SwaggerAssist.getGlobalOperationParameters(swaggerGlobalProperties))
                .ignoredParameterTypes(getIgnoredParameterTypes(ignoredParameterTypes));
    }

    private Class[] getIgnoredParameterTypes(String ignoredParameterTypes) {
        String[] split = ignoredParameterTypes.split(",");
        List<Class> classes = new ArrayList(0);
        for (String clazz : split) {
            if (BoolUtil.notEmpty(clazz)) {
                try {
                    Class<?> aClass = Class.forName(clazz);
                    classes.add(aClass);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return classes.toArray(new Class[0]);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(title)
                .description("Swagger3通用接口文档")
                .version("1.0.0")
//                .contact(new Contact("Jas°", "http://GM.cn/", "1253532233@qq.com"))
                .build();
    }
}
