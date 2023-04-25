package cn.gmlee.tools.api.anno;

import java.lang.annotation.*;

/**
 * Api版本共存注解
 * <p>
 *     当 [headers="version=1.1"] 与 @ApiCoexist(1.1) 在不同方法上使用: @ApiCoexist将失效 (headers优先)
 *     当 [headers="version=1.1"] 与 @ApiCoexist(1.1) 同时在一个方法上使用: 正常 (均符合)
 *     当 [headers="version=1.1"] 与 @ApiCoexist(1.1) 同时在一个方法上使用, 其他方法上只有 [headers="version=1.1"] 则前者生效
 *     当 [headers="version=1.1"] 与 @ApiCoexist(1.2) 同时在一个方法上使用: Not Found Exception
 * </p>
 *
 * @author Jas°
 * @date 2020/8/28 (周五)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiCoexist {
    /**
     * 不写注解和不写版本号将视为默认的版本号
     */
    String DEFAULT_VERSION = "1.0.0";
    /**
     * 用于查找版本号的参数名称常量
     */
    String VERSION_NAME = "version";

    /**
     * Api 版本号
     *
     * @return string
     */
    String value() default DEFAULT_VERSION;
}
