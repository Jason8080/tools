package cn.gmlee.tools.cloud.fallback;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.base.util.ClassUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.NullUtil;
import feign.FeignException;
import feign.hystrix.FallbackFactory;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * The type Generic fallback factory.
 *
 * @param <T> the type parameter
 */
@Data
public class ApiFallbackFactory<T> implements FallbackFactory<T> {

    private Class<?> feignClient;

    @Override
    public T create(Throwable cause) {
        return (T) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{getFeignInterface(cause)},
                (proxy, method, args) -> handleFallback(method, args, cause)
        );
    }

    private Class<?> getFeignInterface(Throwable cause) {
        if (feignClient != null) {
            return feignClient;
        }
        // 从Feign异常中解析出接口
        if (cause instanceof FeignException) {
            try {
                return ((FeignException) cause).request().requestTemplate().feignTarget().type();
            } catch (Exception e) {
                throw new IllegalStateException("Feign target 解析异常");
            }
        }
        // 从异常堆栈中解析出Feign接口
        StackTraceElement[] stackTrace = cause.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            try {
                Class<?> clazz = Class.forName(element.getClassName());
                if (clazz.isInterface() && clazz.getAnnotation(FeignClient.class) != null) {
                    return clazz;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new IllegalStateException("Feign client 解析异常");
    }

    private Object handleFallback(Method method, Object[] args, Throwable cause) {
        Class<?> returnType = NullUtil.get(method.getReturnType(), R.class);
        if (cause instanceof FeignException) {
            String content = ((FeignException) cause).contentUTF8();
            return JsonUtil.toBean(content, returnType);
        }
        Object ret = ExceptionUtil.sandbox(() -> ClassUtil.newInstance(returnType));
        ClassUtil.setValue(ret, "msg", ExceptionUtil.getOriginMsg(cause));
        ClassUtil.setValue(ret, "message", ExceptionUtil.getOriginMsg(cause));
        return ret;
    }
}