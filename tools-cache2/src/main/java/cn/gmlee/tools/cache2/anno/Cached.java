package cn.gmlee.tools.cache2.anno;

import java.lang.annotation.*;

/**
 * 启用缓存.
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cached {
}
