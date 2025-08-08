package cn.gmlee.tools.agent;

import cn.gmlee.tools.agent.conf.MonitorMethodTimingAutoConfiguration;
import cn.gmlee.tools.agent.mod.Monitor;
import org.junit.Test;

public class ByteBuddyTests {

    @Test
    public void testMethod() {
        Monitor ok = new Monitor(null, null, null);
        new MonitorMethodTimingAutoConfiguration(null,null).init();
        ok.elapsedMillis(System.currentTimeMillis());
    }
}
