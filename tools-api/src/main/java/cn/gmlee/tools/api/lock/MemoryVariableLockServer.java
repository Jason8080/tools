package cn.gmlee.tools.api.lock;

import cn.gmlee.tools.api.anno.VariableLock;
import cn.gmlee.tools.base.util.AssertUtil;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        // 加锁是否成功
        boolean success = memoryMap.put(key, getVal(values)) != null;
        log.info("【变量锁】加锁完成: {} {} {}", success, key, getVal(values));
        AssertUtil.isTrue(success, String.format("Please wait while other operations are underway.."));
    }

    public void unlock(VariableLock vl, String... values) {
        String key = getKey(vl, values);
        // 是否解锁成功
        Boolean success = memoryMap.remove(key) != null;
        log.info("【变量锁】解锁完成: {} {} {}", success, key, getVal(values));
    }
}
