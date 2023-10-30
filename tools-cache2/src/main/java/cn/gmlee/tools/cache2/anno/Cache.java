package cn.gmlee.tools.cache2.anno;

import cn.gmlee.tools.cache2.enums.DataType;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {
    /**
     * 源数据.
     * <p>
     *     也可能是API(GET)
     * </p>
     *
     * @return 数据源
     */
    String table();

    /**
     * 外键.
     * <p>
     *     也可能是AIP的筛选字段
     * </p>
     *
     * @return 外键
     */
    String key();

    /**
     * 下载字段.
     *
     * <p>
     * 需要填充什么字段进行展示则配置什么字段;
     * 注意: 如果修饰的是实体类则表示需要填充整条记录; 如果修饰的是集合则表示需要填充所有匹配的列表 (适用二次查询场景).
     * </p>
     *
     * @return 下载字段
     */
    String get() default "";

    /**
     * 上传字段.
     *
     * <p>
     * 提供什么字段进行匹配缓存
     * </p>
     *
     * @return 上传字段
     */
    String put();

    /**
     * 数据类型
     *
     * @return 数据类型
     */
    DataType dataType() default DataType.SQL;

    /**
     * 筛选条件.
     * <p>
     * 支持在当前实体内取值: ${fieldName}.
     * </p>
     *
     * @return
     */
    String where() default "";

    /**
     * 是否开启缓存
     *
     * @return
     */
    boolean enable() default true;

    /**
     * 缓存时间(秒).
     * <p>
     * 永久有效: -1
     * </p>
     *
     * @return
     */
    long expire() default 24 * 3600;
}
