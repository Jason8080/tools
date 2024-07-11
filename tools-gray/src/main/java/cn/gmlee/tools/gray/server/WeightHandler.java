package cn.gmlee.tools.gray.server;

import cn.gmlee.tools.base.alg.weight.Weight;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.gray.mod.App;
import cn.gmlee.tools.gray.mod.Rule;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 权重处理器.
 */
public class WeightHandler extends AbstractGrayHandler {

    private final Map<String, Weight> apps = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Abstract gray handler.
     *
     * @param grayServer the gray server
     */
    public WeightHandler(GrayServer grayServer) {
        super(grayServer);
    }

    @Override
    public String name() {
        return weight;
    }

    @Override
    @SuppressWarnings("all")
    public boolean allow(String app, String num) {
        Weight weight = apps.get(app);
        Integer ratio = getRatio(app);
        if (weight == null || change(app, weight.allowedPercentage)) {
            weight = new Weight(ratio);
            apps.put(app, weight);
        }
        return weight.shouldAllowRequest();
    }

    private boolean change(String app, int num) {
        Integer ratio = getRatio(app);
        if (ratio == null) {
            return false;
        }
        return !ratio.equals(num);
    }

    private Integer getRatio(String app) {
        App application = grayServer.properties.getApps().get(app);
        Map<String, Rule> rules = application.getRules();
        if (BoolUtil.isEmpty(rules)) {
            return null;
        }
        Rule rule = rules.get(name());
        if (BoolUtil.isNull(rule)) {
            return null;
        }
        List<String> content = rule.getContent();
        if (BoolUtil.isEmpty(content)) {
            return null;
        }
        String number = content.get(0);
        if (!BoolUtil.isDigit(number)) {
            return null;
        }
        return Integer.parseInt(number);
    }
}
