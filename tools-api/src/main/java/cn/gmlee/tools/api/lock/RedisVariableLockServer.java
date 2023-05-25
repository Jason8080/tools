package cn.gmlee.tools.api.lock;

import cn.gmlee.tools.api.anno.VariableLock;
import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.redis.util.RedisClient;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * 适用于集群部署.
 */
public class RedisVariableLockServer implements VariableLockServer {

    private static final Logger log = LoggerFactory.getLogger(RedisVariableLockServer.class);

    @Getter
    @Value("${tools.api.variableLock.keyPrefix:tools:api:variableLock:}")
    private String keyPrefix;

    @Autowired
    private RedisClient<String, String> redisClient;


    public void lock(VariableLock vl, String... values) {
        String key = getKey(vl, values);
        // 加锁是否成功
        boolean success = vl.timeout() > 0 ?
                redisClient.setNx(key, getVal(values), vl.timeout()) :
                redisClient.setNx(key, getVal(values));
        log.info("【变量锁】加锁完成: {} {} {}", success, key, getVal(values));
        AssertUtil.isTrue(success, String.format("Please wait while other operations are underway.."));
    }

    public void unlock(VariableLock vl, String... values) {
        String key = getKey(vl, values);
        // 是否解锁成功
        Boolean success = redisClient.delete(key);
        log.info("【变量锁】解锁完成: {} {} {}", success, key, getVal(values));
    }
}
