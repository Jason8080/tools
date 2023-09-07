package cn.gmlee.tools.gray.server;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.gray.conf.GrayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 灰度服务.
 */
public abstract class GrayServer {

    /**
     * The constant log.
     */
    protected static final Logger log = LoggerFactory.getLogger(GrayServer.class);

    @Autowired(required = false)
    private List<GrayHandler> handlers = Collections.emptyList();

    /**
     * 灰度配置.
     */
    public final GrayProperties properties;

    /**
     * Instantiates a new Gray server.
     *
     * @param properties the properties
     */
    public GrayServer(GrayProperties properties) {
        this.properties = properties;
    }

    /**
     * 灰度检查.
     *
     * @param app     the app
     * @param tokens  the tokens
     * @param version the version
     * @return the boolean
     */
    public final boolean check(String app, Map<String, String> tokens, String version) {
        // 外部指定优先
        if (BoolUtil.notEmpty(version)) {
            return true;
        }
        for (GrayHandler handler : handlers) {
            // 获取专属令牌
            String token = tokens.get(handler.name());
            if (BoolUtil.isEmpty(token)) {
                log.info("灰度服务: {} 处理器: {} 令牌是空", app, handler.name());
            }
            // 不支持不处理
            if (!handler.support(app, token)) {
                log.info("灰度服务: {} 处理器: {} 当前关闭", app, handler.name());
                continue;
            }
            // 是否拒绝灰度
            if (handler.allow(app, token)) {
                log.info("灰度服务: {} 处理器: {} 允许灰度", app, handler.name());
                return true;
            }
        }
        return false;
    }

    /**
     * Gets scheme.
     *
     * @param scheme the scheme
     * @return the scheme
     */
    public String getScheme(String scheme) {
        return null;
    }

    /**
     * 解析用户号.
     *
     * @param token the token
     * @return the string
     */
    public abstract String getUserId(String token);

    /**
     * 解析用户名.
     *
     * @param token the token
     * @return the string
     */
    public abstract String getUserName(String token);

    /**
     * 检查是否存在扩展内容中.
     *
     * @param app   the app
     * @param token the token
     * @return the custom content
     */
    public abstract Boolean extend(String app, String token);
}
