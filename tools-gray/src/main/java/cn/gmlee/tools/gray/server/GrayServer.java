package cn.gmlee.tools.gray.server;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.gray.conf.GrayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 灰度服务.
 */
@Slf4j
public class GrayServer {

    @Autowired(required = false)
    private List<GrayHandler> handlers = Collections.emptyList();

    @Autowired
    protected RedisTemplate redisTemplate;

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
     * @param app    the app
     * @param tokens the tokens
     * @return the boolean
     */
    public final boolean check(String app, Map<String, String> tokens) {
        for (GrayHandler handler : handlers) {
            // 获取专属令牌
            String token = tokens.get(handler.name());
            if (BoolUtil.isEmpty(token)) {
                log.info("灰度服务:{} 处理器:{} 令牌是空", app, handler.name());
            }
            // 不支持不处理
            if (!handler.support(app, token)) {
                log.info("灰度服务:{} 处理器:{} 当前关闭", app, handler.name());
                continue;
            }
            // 是否拒绝灰度
            if (handler.allow(app, token)) {
                log.info("灰度服务:{} 处理器:{} 允许灰度", app, handler.name());
                return true;
            }
        }
        return false;
    }

    /**
     * Jwt解析用户号.
     *
     * @param token the token
     * @return the string
     */
    public String getUserId(String token) {
        return token;
    }

    /**
     * Jwt解析用户名.
     *
     * @param token the token
     * @return the string
     */
    public String getUserName(String token) {
        return token;
    }

    /**
     * Gets custom content.
     *
     * @param app the app
     * @param key the key
     * @return the custom content
     */
    public List<String> getCustomContent(String app, String key) {
        ListOperations<String, String> ops = redisTemplate.opsForList();
        return ops.range(key, 0, -1);
    }
}
