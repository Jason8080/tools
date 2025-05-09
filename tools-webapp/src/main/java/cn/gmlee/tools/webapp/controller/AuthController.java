package cn.gmlee.tools.webapp.controller;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.mod.Login;
import cn.gmlee.tools.webapp.service.LoginServer;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 通用鉴权控制器
 *
 * @param <U> 用户泛型
 * @param <P> the type parameter
 * @param <S> 系统设置泛型
 * @param <D> the type parameter
 * @param <C> 用户配置泛型
 * @author Jas °
 * @date 2020 /8/28 (周五)
 */
@Validated
public class AuthController<U, P, S, D, C> extends ParameterController {
    @Resource
    protected LoginServer loginServer;

    protected U user;
    protected Map<String, S> settings;
    protected Set<D> data;
    protected List<C> configs;

    /**
     * Pre handler.
     *
     * @param appId the app id
     * @param token the token
     */
    @ModelAttribute
    public void authPre(
            @RequestHeader(value = "appId", required = false) String appId,
            @RequestHeader(value = "token", required = false) String token
    ) {
        super.parameterPre(appId);
        // 令牌效验
        if (StringUtils.isEmpty(token)) {
            throw new SkillException(XCode.LOGIN_TIMEOUT.code, XCode.LOGIN_TIMEOUT.msg);
        }
        // 登陆效验
        Login<U, S, D, C> old = loginServer.getLogin(token);
        if (Objects.isNull(old)) {
            throw new SkillException(XCode.LOGIN_TIMEOUT.code, XCode.LOGIN_TIMEOUT.msg);
        }
        user = old.getUser();
        configs = old.getConfigs();
        settings = old.getSettings();
        data = old.getData();
    }
}
