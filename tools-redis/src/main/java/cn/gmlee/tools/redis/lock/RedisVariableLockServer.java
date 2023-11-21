package cn.gmlee.tools.redis.lock;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.redis.anno.VariableLock;
import cn.gmlee.tools.redis.util.RedisClient;
import cn.gmlee.tools.redis.util.RedisLock;
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
    private RedisLock redisLock;

    @Autowired
    private RedisClient<String, String> redisClient;


    public void lock(VariableLock vl, String... values) {
        String key = getKey(vl, values);
        if(!vl.lock()){
            // 只需要检查锁
            boolean check = vl.check() && redisClient.exists(key);
            log.info("【变量锁】检锁完成: {} {}", check, key);
            AssertUtil.isFalse(check, vl.message());
            return;
        }
        // 加锁是否成功
        boolean success = redisLock.lock(key, getVal(values), vl.timeout(), vl.spin());
        log.info("【变量锁】加锁完成: {} {}", success, key);
        AssertUtil.isTrue(success, vl.message());
    }

    public void unlock(VariableLock vl, String... values) {
        String key = getKey(vl, values);
        // 是否解锁成功
        Boolean success = redisLock.unlock(key, getVal(values));
        log.info("【变量锁】解锁完成: {} {}", success, key);
    }
}
