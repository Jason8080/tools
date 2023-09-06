package cn.gmlee.tools.gray.server;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.gray.mod.App;
import cn.gmlee.tools.gray.mod.Rule;

import java.util.Map;

/**
 * 用户处理器.
 */
public class UserHandler extends AbstractGrayHandler {
    /**
     * Instantiates a new Abstract gray handler.
     *
     * @param grayServer the gray server
     */
    public UserHandler(GrayServer grayServer) {
        super(grayServer);
    }

    @Override
    public String name() {
        return user;
    }

    @Override
    @SuppressWarnings("all")
    public boolean allow(String app, String token) {
        App application = grayServer.properties.getApps().get(app);
        Map<String, Rule> rules = application.getRules();
        if (BoolUtil.isEmpty(rules)) {
            return false;
        }
        Rule rule = rules.get(name());
        if (BoolUtil.isNull(rule)) {
            return false;
        }
        String name = grayServer.getUserName(token);
        return rule.getContent().contains(name);
    }
}
