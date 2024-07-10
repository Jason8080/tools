package cn.gmlee.tools.gray.server;

import cn.gmlee.tools.base.alg.weight.Weight;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.gray.mod.App;
import cn.gmlee.tools.gray.mod.Rule;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 权重处理器.
 */
public class WeightHandler extends AbstractGrayHandler {

    private final AtomicLong current = new AtomicLong(0);

    private final Map<String, int[]> apps = new ConcurrentHashMap<>();

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
        int[] group = apps.get(app);
        if (group == null || change(app, group)) {
            group = generateGroup(app);
            apps.put(app, group);
        }
        return Weight.request(getIncrementAndGet(), group);
    }

    private boolean change(String app, int[] group) {
        Integer ratio = getRatio(app);
        if (ratio == null) {
            return false;
        }
        return group.length != ratio;
    }

    private long getIncrementAndGet() {
        if (current.get() > Long.MAX_VALUE - 1) {
            current.set(0);
        }
        return current.incrementAndGet();
    }

    private synchronized int[] generateGroup(String app) {
        Integer ratio = getRatio(app);
        if (ratio == null) {
            return null;
        }
        return Weight.groups(ratio)[0];
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
