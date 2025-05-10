package cn.gmlee.tools.webapp.config.login;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录配置.
 */
@Data
@ConfigurationProperties("tools.webapp.login")
public class LoginProperties {
    private List<String> urlPatterns = new ArrayList();
    private List<String> urlExcludes = new ArrayList();

    /**
     * 存储位置.
     */
    protected String tokenPrefix = "REDIS:AUTH:TOKEN_";

    /**
     * 默认有效期、续期时间12小时.
     * <p>
     * 自动续期开启后: 低于12小时有效期将自动增加12小时的有效期;
     * </p>
     */
    protected Long expire = 43200000L;


    /**
     * 必须登陆?Y:N.
     */
    protected Boolean required = false;


    /**
     * 自动续期?Y:N.
     */
    protected Boolean renew = true;
}
