package cn.gmlee.tools.prevent.aspect;

import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.prevent.PreventKeyBuilder;
import cn.gmlee.tools.prevent.annotation.Limit;
import cn.gmlee.tools.prevent.exception.LimitException;
import cn.gmlee.tools.prevent.utils.SlideWindowUtils;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * LimitInterceptor
 *
 * @author James
 * @since 2021-05-27
 */
@Slf4j
public class LimitInterceptor implements MethodInterceptor {

	private SlideWindowUtils slideWindowUtils;

	public LimitInterceptor(SlideWindowUtils slideWindowUtils) {
		this.slideWindowUtils = slideWindowUtils;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		String requestURI = Optional.ofNullable(WebUtil.getRequest())
				.map(HttpServletRequest::getRequestURI)
				.orElse(null);

		Limit limit = invocation.getMethod().getAnnotation(Limit.class);
		long count = limit.count();
		long timeWindow = limit.timeWindow();

		String key = PreventKeyBuilder.buildKey(requestURI, invocation, limit.key(), limit.keyParams());

		if (!slideWindowUtils.isPass(key, count, timeWindow)) {
			throw new LimitException();
		}

		return invocation.proceed();
	}

}
