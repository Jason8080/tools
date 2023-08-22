package cn.gmlee.tools.prevent.config;

import cn.gmlee.tools.prevent.aspect.LimitAnnotationAdvisor;
import cn.gmlee.tools.prevent.aspect.LimitInterceptor;
import cn.gmlee.tools.prevent.utils.SlideWindowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * PreventAutoConfiguration
 *
 * @author James
 * @date 2023-07-26
 */
@Lazy(value = false)
@Configuration
@RequiredArgsConstructor
public class PreventAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public SlideWindowUtils slideWindowUtils(StringRedisTemplate stringRedisTemplate) {
		return new SlideWindowUtils(stringRedisTemplate);
	}

	@Bean
	@ConditionalOnMissingBean
	public LimitInterceptor limitInterceptor(SlideWindowUtils slideWindowUtils) {
		return new LimitInterceptor(slideWindowUtils);
	}

	@Bean
	@ConditionalOnMissingBean
	public LimitAnnotationAdvisor limitAnnotationAdvisor(LimitInterceptor limitInterceptor) {
		return new LimitAnnotationAdvisor(limitInterceptor, Ordered.HIGHEST_PRECEDENCE);
	}

}
