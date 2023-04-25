package cn.gmlee.tools.cache2.anno;

import cn.gmlee.tools.cache2.aspect.CacheAspect;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CacheAspect.class})
public @interface EnableCache2 {
}
