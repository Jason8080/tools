package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Function;
import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.mod.Login;

import java.util.HashMap;
import java.util.Map;

/**
 * 登陆对象绑定到当前线程的工具.
 *
 * @author Jas °
 * @date 2021 /10/21 (周四)
 */
public class LoginUtil {

    /**
     * 存登陆用户的地方
     */
    private static final ThreadLocal<Login> users = new InheritableThreadLocal();


    /**
     * 获取本次请求的登陆对象 (没有登陆返回空不会抛出异常).
     *
     * @param required the required
     * @return the login
     */
    public static Login get(boolean required) {
        if (required && users.get() == null) {
            throw new SkillException(XCode.LOGIN_TIMEOUT.code, "请重新登陆");
        }
        return users.get();
    }

    /**
     * 获取本次请求的登陆对象 (没有登陆返回空不会抛出异常).
     *
     * @return the login
     */
    public static Login get() {
        return get(false);
    }


    /**
     * 绑定登陆对象到线程中.
     *
     * @param login the login
     */
    public static void set(Login login) {
        users.set(login);
    }

    /**
     * 获取登陆用户 (没有登陆将抛出异常).
     *
     * @param <U>      the type parameter
     * @param clazz    the clazz
     * @param required the required
     * @return the user
     */
    public static <U> U getUser(Class<U> clazz, boolean required) {
        U u = getUser(clazz);
        if (required && u == null) {
            return ExceptionUtil.cast(XCode.LOGIN_TIMEOUT);
        }
        return u;
    }

    /**
     * 获取登陆用户 (没有登陆将抛出异常).
     *
     * @param <U>   the type parameter
     * @param clazz the clazz
     * @return the u
     */
    public static <U> U getUser(Class<U> clazz) {
        Login login = users.get();
        if (login == null) {
            return null;
        }
        Object user = login.getUser();
        if (user == null) {
            return null;
        }
        if (BoolUtil.isParentClass(clazz, user.getClass())) {
            return (U) user;
        }
        return JsonUtil.convert(user, clazz);
    }

    /**
     * 执行函数并保存返回结果到当前线程 .
     * <p>
     * 优先线程上的数据, 线程上没有在执行函数获取.
     * </p>
     *
     * @param <S>   the type parameter
     * @param key   the key
     * @param clazz the clazz
     * @param run   the run
     * @return the
     */
    public static <S> S setting(String key, Class<S> clazz, Function.Zero2r<S> run) {
        // 优先缓存
        S setting = getSetting(key, clazz);
        if (setting == null) {
            try {
                setting = run.run();
            } catch (Throwable e) {
                return ExceptionUtil.cast(XCode.SERVER_CODE);
            }
            // 首次使用缓存到线程中
            setSetting(key, setting);
        }
        return setting;
    }

    /**
     * 获取设置的内容.
     *
     * @param <S>   the type parameter
     * @param key   the key
     * @param clazz the clazz
     * @param def   the def
     * @return the settings
     */
    public static <S> S getSetting(String key, Class<S> clazz, S def) {
        S setting = getSetting(key, clazz);
        return NullUtil.get(setting, def);
    }

    /**
     * 获取设置的内容.
     *
     * @param <S>   the type parameter
     * @param key   the key
     * @param clazz the clazz
     * @return the settings
     */
    public static <S> S getSetting(String key, Class<S> clazz) {
        Login login = users.get();
        if (login == null) {
            return null;
        }
        Map settings = login.getSettings();
        if (BoolUtil.isEmpty(settings)) {
            return null;
        }
        Object setting = settings.get(key);
        if (setting == null) {
            return null;
        }
        if (BoolUtil.isParentClass(clazz, setting.getClass())) {
            return (S) setting;
        }
        return JsonUtil.convert(setting, clazz);
    }

    /**
     * 设置新内容.
     *
     * @param <S> the type parameter
     * @param key the key
     * @param val the val
     * @return 无论如何返回原值 setting
     */
    public static <S> S setSetting(String key, S val) {
        Login login = users.get();
        if (login == null) {
            return val;
        }
        Map settings = login.getSettings();
        if (settings == null) {
            Map map = new HashMap();
            map.put(key, val);
            login.setSettings(map);
        } else {
            settings.put(key, val);
        }
        return val;
    }

    /**
     * 清理线程垃圾.
     */
    public static void remove() {
        users.remove();
    }
}
