package cn.gmlee.tools.agent.assist;

import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.agent.trigger.ByteBuddyTrigger;
import cn.gmlee.tools.agent.trigger.TimeoutTrigger;
import cn.gmlee.tools.base.enums.Function;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.spring.util.IocUtil;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 触发器辅助工具.
 */
public class TriggerAssist {
    /**
     * 触发器.
     *
     * @param watcher the watcher
     * @param fun     the fun
     */
    public static void register(Watcher watcher, BiConsumer<ByteBuddyTrigger, Watcher> fun) {
        Map<String, ByteBuddyTrigger> beanMap = IocUtil.getBeanMap(ByteBuddyTrigger.class);
        if(BoolUtil.isEmpty(beanMap)){
            return;
        }
        for (Map.Entry<String, ByteBuddyTrigger> next : beanMap.entrySet()) {
            String key = next.getKey();
            ByteBuddyTrigger trigger = next.getValue();
            if (trigger == null) {
                continue;
            }
            ExceptionUtil.sandbox(() -> fun.accept(trigger, watcher));
        }
    }

    /**
     * 触发器.
     *
     * @param thread   the thread
     * @param watchers the watchers
     */
    public static void timout(Thread thread, List<Watcher> watchers) {
        Map<String, TimeoutTrigger> beanMap = IocUtil.getBeanMap(TimeoutTrigger.class);
        if(BoolUtil.isEmpty(beanMap)){
            return;
        }
        for (Map.Entry<String, TimeoutTrigger> next : beanMap.entrySet()) {
            String key = next.getKey();
            TimeoutTrigger trigger = next.getValue();
            if (trigger == null) {
                continue;
            }
            ExceptionUtil.sandbox(() -> trigger.handler(thread, watchers));
        }
    }
}
