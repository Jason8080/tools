package cn.gmlee.tools.gray.server;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.gray.mod.App;
import cn.gmlee.tools.gray.mod.Rule;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 定制处理器.
 */
@Slf4j
public class CustomHandler extends AbstractGrayHandler {
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
        // 本地匹配
        boolean cofRet = matchingCof(app, token);
        // 扩展匹配
        boolean remoteRet = matchingRemote(app, token);
        // 二选一
        return cofRet || remoteRet;
    }

    private boolean matchingRemote(String app, String token) {
        Boolean extend = grayServer.extend(app, token);
        if (!Boolean.TRUE.equals(extend)) {
            log.debug("灰度服务: {} 处理程序: {} 没有扩展", app, name());
            return false;
        }
        return true;
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
        String id = grayServer.getUserId(token);
        return rule.getContent().contains(id);
    }
}
