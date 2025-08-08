package cn.gmlee.tools.agent;

import cn.gmlee.tools.agent.conf.MonitorMethodTimingAutoConfiguration;
import cn.gmlee.tools.agent.mod.Watcher;
import org.junit.Test;

public class ByteBuddyTests {

    @Test
    public void testMethod() {
        Watcher ok = new Watcher(null, null, null);
        new MonitorMethodTimingAutoConfiguration(null,null).init();
        ok.elapsedMillis(System.currentTimeMillis());
    }
}
