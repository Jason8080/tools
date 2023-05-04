package cn.gmlee.tools.webapp.controller;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.util.ClassUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.redis.util.RedisClient;
import cn.gmlee.tools.webapp.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 通用鉴权控制器
 *
 * @param <User> the type parameter
 * @author Jas°
 * @date 2020 /8/28 (周五)
 */
@Validated
public class SimpleAuthController<User> extends ParameterController {
    @Resource
    protected LoginService loginService;
    @Autowired(required = false)
    protected RedisClient<String, User> rc;

    protected User user;

    /**
     * Pre handler.
     *
     * @param appId the app id
     * @param token the token
     */
    @ModelAttribute
    public void preHandler(
            @RequestHeader(value = "appId", required = false) String appId,
            @RequestHeader(value = "token", required = false) String token
    ) {
        super.parameterPre(appId);
        // 令牌效验
        if (StringUtils.isEmpty(token)) {
            throw new SkillException(XCode.ACCOUNT_AUTH7001.code, XCode.ACCOUNT_AUTH7001.msg);
        }
        // 登陆效验
        user = getUser(token);
        if (Objects.isNull(user)) {
            throw new SkillException(XCode.ACCOUNT_AUTH7001.code, XCode.ACCOUNT_AUTH7001.msg);
        }
        // 兼容程序员的错误存储方式
        Class<User> genericClass = ClassUtil.getGenericClass(this);
        if (!user.getClass().equals(genericClass)) {
            user = JsonUtil.convert(user, genericClass);
        }
    }

    public User getUser(String token) {
        return rc.get(loginService.getTokenPrefix().concat(token));
    }
}
