package cn.gmlee.tools.cache2.config;


import cn.gmlee.tools.cache2.aspect.CacheAspect;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Import;

@Import(CacheAspect.class)
@AutoConfigureAfter(HandlerCacheAutoConfiguration.class)
public class AspectCacheAutoConfiguration {

}
