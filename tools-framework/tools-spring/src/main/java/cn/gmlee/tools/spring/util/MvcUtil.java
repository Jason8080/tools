package cn.gmlee.tools.spring.util;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.EnumUtil;
import cn.gmlee.tools.base.util.ProxyUtil;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * SpringMvc控制器工具.
 */
public class MvcUtil {
    /**
     * 注册映射处理器.
     *
     * @param <C>        the 处理器
     * @param uri        the 路径
     * @param rm         the 方法
     * @param controller the 源对象
     */
    public static <C> void register(String uri, String rm, Class<C> controller) {
        RequestMethod requestMethod = EnumUtil.name(rm, RequestMethod.class);
        AssertUtil.notNull(requestMethod, "Register controller method is not exist");
        register(uri, requestMethod, controller);
    }

    /**
     * 注册映射处理器.
     *
     * @param <C>        the 处理器
     * @param uri        the 路径
     * @param rm         the 方法
     * @param controller the 源对象
     */
    public static <C> void register(String uri, RequestMethod rm, Class<C> controller) {
        register(new RequestMappingInfo(
                new PatternsRequestCondition(uri),
                new RequestMethodsRequestCondition(rm),
                null,
                null,
                null,
                null,
                null
        ), controller);
    }


    /**
     * 注册映射处理器.
     *
     * @param <C>        the 处理器
     * @param info       the 请求信息
     * @param controller the 源对象
     */
    public static <C> void register(RequestMappingInfo info, Class<C> controller) {
        // 创建代理对象
        C c = ProxyUtil.CglibProxy(controller, (Object obj, Method method, Object[] args, MethodProxy proxy) -> proxy.invokeSuper(obj, args));
        // 获取映射集合
        RequestMappingHandlerMapping handlerMapping = IocUtil.getBean(RequestMappingHandlerMapping.class);
        AssertUtil.notNull(handlerMapping, "Ioc create bean error: RequestMappingHandlerMapping");
        // 注册动态接口
        HandlerMethod handler = new HandlerMethod(c, getMethod(controller));
        handlerMapping.registerMapping(
                info, // 映射条件
                handler.getBean(), // 处理对象
                handler.getMethod() // 处理函数
        );
    }

    private static <C> Method getMethod(Class<C> controller) {
        // 检查方法数量
        AssertUtil.notEmpty(controller.getMethods(), String.format("%s the methods is empty!", controller.getName()));
        for (Method method : controller.getMethods()){
            if(method.isAccessible()){
                return method;
            }
        }
        return controller.getMethods()[0];
    }
}
