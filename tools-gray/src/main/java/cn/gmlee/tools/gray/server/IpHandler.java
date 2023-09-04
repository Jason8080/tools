package cn.gmlee.tools.gray.server;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.UrlUtil;
import cn.gmlee.tools.gray.mod.App;
import cn.gmlee.tools.gray.mod.Rule;

import java.util.Map;

/**
 * 地址处理器.
 */
public class IpHandler extends AbstractGrayHandler {
    /**
     * Instantiates a new Abstract gray handler.
     *
     * @param grayServer the gray server
     */
    public IpHandler(GrayServer grayServer) {
        super(grayServer);
    }

    @Override
    public String name() {
        return ip;
    }

    @Override
    @SuppressWarnings("all")
    public boolean allow(String app, String ip) {
        App application = grayServer.properties.getApps().get(app);
        Map<String, Rule> rules = application.getRules();
        if(BoolUtil.isEmpty(rules)){
            return false;
        }
        Rule rule = rules.get(name());
        if(BoolUtil.isNull(rule)){
            return false;
        }
        return UrlUtil.matchOne(rule.getContent(), ip);
    }
}
