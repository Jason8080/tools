package cn.gmlee.tools.gray.server;

import cn.gmlee.tools.gray.assist.PropAssist;
import cn.gmlee.tools.gray.mod.Rule;

/**
 * 处理器.
 */
public abstract class AbstractGrayHandler implements GrayHandler {
    /**
     * The Gray server.
     */
    protected final GrayServer grayServer;

    /**
     * Instantiates a new Abstract gray handler.
     *
     * @param grayServer the gray server
     */
    protected AbstractGrayHandler(GrayServer grayServer) {
        this.grayServer = grayServer;
    }

    @Override
    public boolean support(String app, String token) {
        String name = name();
        Rule rule = PropAssist.getRules(grayServer.properties, app).get(name);
        return rule != null ? enable(rule) : false;
    }

    private static Boolean enable(Rule rule) {
        // 显式开启才进行处理
        return Boolean.TRUE.equals(rule.getEnable());
    }
}
