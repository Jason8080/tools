package cn.gmlee.tools.webapp.controller;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.mod.Login;
import cn.gmlee.tools.webapp.service.LoginService;
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
 * @param <S> 系统设置泛型
 * @param <D> the type parameter
 * @param <C> 用户配置泛型
 * @author Jas°
 * @date 2020 /8/28 (周五)
 */
@Validated
public class AuthController<U, P, S, D, C> extends ParameterController {
    @Resource
    protected LoginService loginService;

    protected U user;
    protected P permissions;
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
            throw new SkillException(XCode.ACCOUNT_AUTH7001.code, XCode.ACCOUNT_AUTH7001.msg);
        }
        // 登陆效验
        Login<U, P, S, D, C> old = loginService.getLogin(token);
        if (Objects.isNull(old)) {
            throw new SkillException(XCode.ACCOUNT_AUTH7001.code, XCode.ACCOUNT_AUTH7001.msg);
        }
        user = old.getUser();
        permissions = old.getPermissions();
        configs = old.getConfigs();
        settings = old.getSettings();
        data = old.getData();
    }
}
