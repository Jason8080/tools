package cn.gmlee.tools.ds.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 谨记: 使注解生效必须通过代理对象调用.
 * <p>
 *     警告: 开启事务后, 不会再切换数据源; 因此事务内部的注解无效.
 * </p>
 *
 * @author Jas°
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ds {
    /**
     * 使用指定数据源操作.
     *
     * @return the string
     */
    String[] value() default "";
}
