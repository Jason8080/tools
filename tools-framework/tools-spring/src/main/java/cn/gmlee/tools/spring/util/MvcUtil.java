package cn.gmlee.tools.spring.util;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.ClassUtil;
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
     * 已弃用 (无法精准定位处理函数).
     *
     * @param <C>        the type parameter
     * @param uri        the uri
     * @param rm         the rm
     * @param controller the controller
     */
    @Deprecated
    public static <C> void register(String uri, String rm, C controller) {
        RequestMethod requestMethod = EnumUtil.name(rm, RequestMethod.class);
        AssertUtil.notNull(requestMethod, "Register controller method is not exist");
        register(uri, requestMethod, controller);
    }

    /**
     * Register.
     *
     * @param <C>        the type parameter
     * @param uri        the uri
     * @param rm         the rm
     * @param controller the controller
     * @param method     the method
     */
    public static <C> void register(String uri, String rm, C controller, String method) {
        RequestMethod requestMethod = EnumUtil.name(rm, RequestMethod.class);
        AssertUtil.notNull(requestMethod, "Register controller method is not exist");
        register(uri, requestMethod, controller, method);
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
     * 已弃用 (无法精准定位处理函数).
     *
     * @param <C>        the type parameter
     * @param uri        the uri
     * @param rm         the rm
     * @param controller the controller
     */
    @Deprecated
    public static <C> void register(String uri, RequestMethod rm, C controller) {
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
     * Register.
     *
     * @param <C>        the type parameter
     * @param uri        the uri
     * @param rm         the rm
     * @param controller the controller
     * @param method     the method
     */
    public static <C> void register(String uri, RequestMethod rm, C controller, String method) {
        register(new RequestMappingInfo(
                new PatternsRequestCondition(uri),
                new RequestMethodsRequestCondition(rm),
                null,
                null,
                null,
                null,
                null
        ), controller, method);
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
        register(info, ProxyUtil.CglibProxy(controller, (Object obj, Method method, Object[] args, MethodProxy proxy) -> proxy.invokeSuper(obj, args)));
    }

    /**
     * 已弃用 (无法精准定位处理函数).
     *
     * @param <C>  the type parameter
     * @param info the info
     * @param c    the c
     */
    @Deprecated
    public static <C> void register(RequestMappingInfo info, C c) {
        AssertUtil.notNull(c, "对象是空");
        register(info, c, getMethod(c.getClass()));
    }

    /**
     * Register.
     *
     * @param <C>    the type parameter
     * @param info   the info
     * @param c      the c
     * @param method the method
     */
    public static <C> void register(RequestMappingInfo info, C c, String method) {
        AssertUtil.notNull(c, "对象是空");
        AssertUtil.notEmpty(method, "方法名称是空");
        Method m = ClassUtil.getMethodMap(c).get(method);
        register(info, c, m);
    }

    /**
     * Register.
     *
     * @param <C>    the type parameter
     * @param info   the info
     * @param c      the c
     * @param method the method
     */
    public static <C> void register(RequestMappingInfo info, C c, Method method) {
        // 参数合法校验
        AssertUtil.notNull(c, "对象是空");
        AssertUtil.notNull(method, "处理方法不存在");
        // 获取映射集合
        RequestMappingHandlerMapping handlerMapping = IocUtil.getBean(RequestMappingHandlerMapping.class);
        AssertUtil.notNull(handlerMapping, "Ioc create bean error: RequestMappingHandlerMapping");
        // 注册动态接口
        HandlerMethod handler = new HandlerMethod(c, method);
        handlerMapping.registerMapping(
                info, // 映射条件
                handler.getBean(), // 处理对象
                handler.getMethod() // 处理函数
        );
    }

    private static <C> Method getMethod(Class<C> controller) {
        // 检查方法数量
        AssertUtil.notEmpty(controller.getDeclaredMethods(), String.format("%s the methods is empty!", controller.getName()));
        return controller.getMethods()[0];
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 注销映射处理器.
     *
     * @param <C> the type parameter
     * @param uri the uri
     * @param rm  the rm
     */
    public static <C> void unregister(String uri, String rm) {
        RequestMethod requestMethod = EnumUtil.name(rm, RequestMethod.class);
        AssertUtil.notNull(requestMethod, "Register controller method is not exist");
        unregister(uri, requestMethod);
    }

    /**
     * 注销映射处理器.
     *
     * @param <C> the type parameter
     * @param uri the uri
     * @param rm  the rm
     */
    public static <C> void unregister(String uri, RequestMethod rm) {
        unregister(new RequestMappingInfo(
                new PatternsRequestCondition(uri),
                new RequestMethodsRequestCondition(rm),
                null,
                null,
                null,
                null,
                null)
        );
    }

    /**
     * 注销映射处理器.
     *
     * @param <C>  the type parameter
     * @param info the info
     */
    public static <C> void unregister(RequestMappingInfo info) {
        RequestMappingHandlerMapping handlerMapping = IocUtil.getBean(RequestMappingHandlerMapping.class);
        AssertUtil.notNull(handlerMapping, "Ioc create bean error: RequestMappingHandlerMapping");
        handlerMapping.unregisterMapping(info);
    }

    // -----------------------------------------------------------------------------------------------------------------


    /**
     * 重置映射处理器.
     *
     * @param <C>        the type parameter
     * @param uri        the uri
     * @param rm         the rm
     * @param controller the controller
     */
    public static <C> void resetRegister(String uri, String rm, Class<C> controller) {
        unregister(uri, rm);
        register(uri, rm, controller);
    }

    /**
     * 已弃用 (无法精准定位处理函数).
     *
     * @param <C>        the type parameter
     * @param uri        the uri
     * @param rm         the rm
     * @param controller the controller
     */
    @Deprecated
    public static <C> void resetRegister(String uri, String rm, C controller) {
        unregister(uri, rm);
        register(uri, rm, controller);
    }

    /**
     * Reset register.
     *
     * @param <C>        the type parameter
     * @param uri        the uri
     * @param rm         the rm
     * @param controller the controller
     * @param method     the method
     */
    public static <C> void resetRegister(String uri, String rm, C controller, String method) {
        unregister(uri, rm);
        register(uri, rm, controller, method);
    }

    // -----------------------------------------------------------------------------------------------------------------
}
