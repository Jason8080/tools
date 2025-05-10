package cn.gmlee.tools.webapp.service;

import cn.gmlee.tools.base.enums.Function;
import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.mod.Login;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.IdUtil;
import cn.gmlee.tools.base.util.QuickUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;

/**
 * 通用登陆常量
 *
 * @param <U> 用户
 * @param <P> 权限
 * @param <S> 设置
 * @param <D> 数据
 * @param <C> 配置
 * @author Jas °
 * @date 2021 /1/15 (周五)
 */
public interface LoginServer<U, P, S, D, C> {
    /**
     * 将请求中的token代表的登陆对象保存到线程中.
     *
     * @param request the request
     * @throws SkillException the skill exception
     */
    void add(HttpServletRequest request) throws SkillException;

    /**
     * 绑定到线程中.
     *
     * @param token the token
     * @throws SkillException the skill exception
     */
    void set(String token) throws SkillException;

    /**
     * 获取登陆对象 (可用于覆盖).
     *
     * @param token the token
     * @return the login
     */
    Login<U, S, D, C> getLogin(String token);

    /**
     * 续期/续租 (默认开启)
     *
     * @param token the token
     */
    void renew(String token);

    /**
     * 登陆.
     *
     * @param login      the login
     * @param successful the successful
     */
    void login(Login<U, S, D, C> login, Function.One<Login>... successful);

    /**
     * 登出.
     *
     * @param tokens the tokens
     * @return the long
     */
    Number logout(String... tokens);

    /**
     * 登出.
     *
     * @param tokens the tokens
     * @return the long
     */
    Number logout(Collection<String> tokens);

    /**
     * 登陆成功(回调).
     * <p>
     * 可以重写也可以面向过程编程
     * </p>
     *
     * @param login      the login
     * @param successful the successful
     */
    void success(Login<U, S, D, C> login, Function.One<Login>... successful);

    /**
     * 保存登陆对象 (用于覆盖).
     *
     * @param login the login
     * @return the string
     */
    boolean generate(Login<U, S, D, C> login);

    /**
     * 生成Token (用于覆盖).
     *
     * @param login the login
     * @return the string
     */
    String generator(Login<U, S, D, C> login);
}
