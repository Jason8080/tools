package cn.gmlee.tools.webapp.service;

import cn.gmlee.tools.base.enums.Function;
import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.LoginException;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.mod.Login;
import cn.gmlee.tools.base.util.*;
import cn.gmlee.tools.redis.util.RedisClient;
import cn.gmlee.tools.webapp.config.login.LoginProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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
@Data
@Slf4j
public class LoginService<U, P, S, D, C> implements LoginServer<U, P, S, D, C> {
    @Autowired
    protected LoginProperties lp;

    /**
     * 注入RedisClient.
     */
    @Autowired(required = false)
    protected RedisClient<String, Login<U, S, D, C>> rc;

    /**
     * 将请求中的token代表的登陆对象保存到线程中.
     *
     * @param request the request
     * @throws SkillException the skill exception
     */
    public void add(HttpServletRequest request) throws SkillException {
        set(WebUtil.getParameter("token", request));
    }

    /**
     * 绑定到线程中.
     *
     * @param token the token
     * @throws SkillException the skill exception
     */
    public void set(String token) throws SkillException {
        Login<U, S, D, C> login = getLogin(NullUtil.get(token));
        if (lp.getRenew() && login == null) {
            ExceptionUtil.cast(XCode.LOGIN_TIMEOUT);
        } else if (login != null) {
            LoginUtil.set(login);
        }
    }

    /**
     * 获取登陆对象 (可用于覆盖).
     *
     * @param token the token
     * @return the login
     */
    public Login<U, S, D, C> getLogin(String token) {
        // 自动续期
        ExceptionUtil.sandbox(() -> renew(token));
        return rc.get(lp.getTokenPrefix().concat(token));
    }

    /**
     * 续期/续租 (默认开启)
     *
     * @param token the token
     */
    public void renew(String token) {
        if (lp.getRenew()) {
            Long old = rc.getExpire(lp.getTokenPrefix().concat(token));
            // 时间充足将不续期
            if(BoolUtil.gt(old, lp.getExpire())){
                return;
            }
            // 当过期时间为零则: 已过期;
            // 该方案有不断续期效果, 不会一次性续足; 缺点是不好计算剩余时间
            long newExpire = old * 2;
            // 该方案有一次续足效果, 优点是明确知道续期后的过期时间; long newExpire = (old * 1000) + expire;
            rc.addExpire(lp.getTokenPrefix().concat(token), newExpire);
        }
    }

    /**
     * 登陆.
     *
     * @param login      the login
     * @param successful the successful
     */
    public void login(Login<U, S, D, C> login, Function.One<Login>... successful) {
        for (int i = 0; i < Int.THREE; i++) {
            if (generate(login)) {
                success(login, successful);
                log.debug("登录成功: {}", login);
                return;
            }
        }
        ExceptionUtil.cast(LoginException.newly("令牌连续3次生成失败"));
    }

    /**
     * 登出.
     *
     * @param tokens the tokens
     * @return the long
     */
    public Long logout(String... tokens) {
        if (BoolUtil.notEmpty(tokens)) {
            return rc.delete(Arrays.asList(tokens));
        }
        return Int.ZERO.longValue();
    }

    /**
     * 登出.
     *
     * @param tokens the tokens
     * @return the long
     */
    public Long logout(Collection<String> tokens) {
        if (BoolUtil.notEmpty(tokens)) {
            return rc.delete(tokens);
        }
        return Int.ZERO.longValue();
    }

    /**
     * 登陆成功(回调).
     * <p>
     * 可以重写也可以面向过程编程
     * </p>
     *
     * @param login      the login
     * @param successful the successful
     */
    public void success(Login<U, S, D, C> login, Function.One<Login>... successful) {
        QuickUtil.notEmpty(successful, array -> {
            for (Function.One<Login> one : successful) {
                one.run(login);
            }
        });
    }

    /**
     * 保存登陆对象 (用于覆盖).
     *
     * @param login the login
     * @return the string
     */
    public boolean generate(Login<U, S, D, C> login) {
        QuickUtil.isEmpty(login.getToken(), x -> login.setToken(generator(login)));
        return rc.setNx(lp.getTokenPrefix().concat(login.getToken()), login, lp.getExpire());
    }

    /**
     * 生成Token (用于覆盖).
     *
     * @param login the login
     * @return the string
     */
    public String generator(Login<U, S, D, C> login) {
        // 可用Jwt生成
        return IdUtil.uuidReplaceUpperCase();
    }
}
