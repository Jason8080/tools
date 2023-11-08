package cn.gmlee.tools.redis.lock;

import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.redis.anno.VariableLock;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * 仅适用于单机部署.
 */
public class MemoryVariableLockServer implements VariableLockServer {

    private static final Logger log = LoggerFactory.getLogger(MemoryVariableLockServer.class);

    @Getter
    @Value("${tools.api.variableLock.keyPrefix:tools:api:variableLock:}")
    private String keyPrefix;

    private Map<String, String> memoryMap = new ConcurrentHashMap<>();


    public void lock(VariableLock vl, String... values) {
        String key = getKey(vl, values);
        if(!vl.lock()){
            // 只需要检查锁
            boolean check = vl.check() && memoryMap.containsKey(key) && memoryMap.containsValue(getVal(values));
            AssertUtil.isFalse(check, vl.message());
            return;
        }
        // 最多自旋10000次 ≈ 30s
        AtomicInteger count = new AtomicInteger(10000);
        while (vl.spin() && count.decrementAndGet() > 0){
            // 失败继续自旋
            if(memoryMap.containsKey(key)) {
                ExceptionUtil.sandbox(() -> sleep(Int.THREE));
                continue;
            }
            log.info("【变量锁】加锁完成: {} {} {}", true, key, getVal(values));
            // 成功记录新锁
            memoryMap.put(key, getVal(values));
            return;
        }
        // 加锁是否成功
        boolean success = !memoryMap.containsKey(key);
        log.info("【变量锁】加锁完成: {} {} {}", success, key, getVal(values));
        AssertUtil.isTrue(success, vl.message());
        memoryMap.put(key, getVal(values));
    }

    public void unlock(VariableLock vl, String... values) {
        String key = getKey(vl, values);
        // 是否解锁成功
        memoryMap.remove(key);
        log.info("【变量锁】解锁完成: {} {} {}", true, key, getVal(values));
    }
}
