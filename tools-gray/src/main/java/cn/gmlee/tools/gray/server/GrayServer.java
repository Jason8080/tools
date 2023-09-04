package cn.gmlee.tools.gray.server;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.gray.assist.PropAssist;
import cn.gmlee.tools.gray.conf.GrayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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
    public boolean check(String app, Map<String, String> tokens) {
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
            if (!handler.allow(app, token)) {
                log.info("灰度服务:{} 处理器:{} 拒绝灰度", app, handler.name());
                return false;
            }
        }
        return true;
    }
}
