package cn.gmlee.tools.gray.server;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.gray.mod.App;
import cn.gmlee.tools.gray.mod.Rule;
import cn.gmlee.tools.redis.util.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

/**
 * 定制处理器.
 */
@Slf4j
public class CustomHandler extends AbstractGrayHandler {

    @Autowired
    private RedisClient<String, String> redisClient;

    /**
     * Instantiates a new Abstract gray handler.
     *
     * @param grayServer the gray server
     */
    public CustomHandler(GrayServer grayServer) {
        super(grayServer);
    }

    @Override
    public String name() {
        return custom;
    }

    @Override
    public boolean allow(String app, String token) {
        boolean idsRet = matchingCof(app, token);
        boolean redisRet = matchingRedis(app, token);
        return idsRet || redisRet;
    }

    private boolean matchingRedis(String app, String token) {
        String key = grayServer.properties.getKey();
        String json = redisClient.get(String.format(key, app));
        List<String> list = JsonUtil.toBean(json, List.class);
        if (list == null) {
            log.info("灰度服务: {} 处理器: {} 远程尚未登记灰度名单", app, name());
            return false;
        }
        String userId = grayServer.jwtUserId(token);
        return list.contains(userId);
    }

    @SuppressWarnings("all")
    private boolean matchingCof(String app, String token) {
        App application = grayServer.properties.getApps().get(app);
        Map<String, Rule> rules = application.getRules();
        if (BoolUtil.isEmpty(rules)) {
            return false;
        }
        Rule rule = rules.get(name());
        if (BoolUtil.isNull(rule)) {
            return false;
        }
        return rule.getContent().contains(token);
    }
}
