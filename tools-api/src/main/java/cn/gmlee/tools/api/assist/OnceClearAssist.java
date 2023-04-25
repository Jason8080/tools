package cn.gmlee.tools.api.assist;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Jas°
 * @date 2020/11/4 (周三)
 */
public class OnceClearAssist implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(OnceClearAssist.class);
    private static final Thread thread = new Thread(new OnceClearAssist(), "防重放数据自动清理定时任务");

    private OnceClearAssist() {
    }

    /**
     * 定时任务执行间隔
     */
    private static long EXECUTE_INTERVAL_SECONDS = 60;

    @Override
    @SneakyThrows
    public void run() {
        while (true) {
            autoClear();
            // 睡眠指定时间
            Thread.sleep(EXECUTE_INTERVAL_SECONDS * 1000);
        }
    }

    public static void autoClear() {
        Enumeration<String> keys = OnceAssist.onceMap.keys();
        while (keys.hasMoreElements()) {
            autoClear(keys.nextElement());
        }
    }

    @SuppressWarnings("all")
    public static void autoClear(String url) {
        Map<String, Long> tokenMap = OnceAssist.onceMap.get(url);
        if (tokenMap != null) {
            Iterator<Map.Entry<String, Long>> it = tokenMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> next = it.next();
                Long offTime = next.getValue();
                // 过期就删掉该用户的限制数据
                if (System.currentTimeMillis() > offTime) {
                    // log.info(String.format("防重放数据已清理: token:[%s] -> url:[%s]", next.getKey(), url));
                    it.remove();
                }
            }
        }
    }

    public static void start() {
        thread.start();
    }
}
