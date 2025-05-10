package cn.gmlee.tools.base.mod;

import cn.gmlee.tools.base.define.Payload;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 通用登陆用户信息对象
 *
 * @param <U> 用户泛型
 * @param <S> the type parameter
 * @param <D> the type parameter
 * @param <C> 配置泛型
 * @author Jas °
 * @date 2020 /9/27 (周日)
 */
@Data
public class Login<U, S, D, C> implements Payload, Serializable {
    /**
     * The Login time.
     */
    protected LocalDateTime loginTime = LocalDateTime.now();
    /**
     * The Identifier.
     */
    protected String identifier;
    /**
     * The Token.
     */
    protected String token;
    /**
     * The Exp.
     */
    protected Long exp;
    /**
     * The Uid.
     */
    protected Long uid;
    /**
     * The Account.
     */
    protected String account;
    /**
     * The Username.
     */
    protected String username;
    /**
     * The Portrait.
     */
    protected String portrait;
    /**
     * The User type.
     */
    protected String userType;
    /**
     * The Client type.
     */
    protected String clientType;
    /**
     * The Status.
     */
    protected Integer status;
    /**
     * The Lon.
     */
    protected String lon;
    /**
     * The Lat.
     */
    protected String lat;
    /**
     * The City.
     */
    protected String city;
    /**
     * The Login ip.
     */
    protected String loginIp;
    /**
     * The Login url.
     */
    protected String loginUrl;
    /**
     * The App id.
     */
    protected String appId;
    /**
     * The Open id.
     */
    protected String openId;

    /**
     * 唯一用户信息: 用户在本次登录中的用户信息
     */
    protected U user;
    /**
     * 去重用户设置: 用户在本次登录中的临时设置信息
     */
    protected Map<String, S> settings;
    /**
     * 无序用户数据: 用户在本次登陆产生的缓存数据 (用于不需要存储但需要登陆查看的信息)
     */
    protected Set<D> data;
    /**
     * 有序系统配置: 用户在本次登陆应用的系统配置信息 (用于系统配置变更后不能影响登陆中的用户)
     */
    protected List<C> configs;
}
