package cn.gmlee.tools.redis.util;

import cn.gmlee.tools.redis.anno.VariableLock;
import cn.gmlee.tools.redis.lock.VariableLockServer;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;

/**
 * 变量锁.
 *
 * <p>手动加解锁工具.</p>
 */
@RequiredArgsConstructor
public class VariableLockUtil {

    private final VariableLockServer variableLockServer;

    /**
     * 加锁.
     *
     * @param key the key
     * @param val the val
     */
    public void lock(String key, String val) {
        VariableLock vl = getVariableLock(key);
        variableLockServer.lock(vl, val);
    }

    /**
     * 加锁.
     *
     * <p>key和val的数量需要一致</p>
     *
     * @param key the key
     * @param val the val
     */
    public void lock(String[] key, String[] val) {
        VariableLock vl = getVariableLock(key);
        variableLockServer.lock(vl, val);
    }

    /**
     * 加锁.
     *
     * @param key     the key
     * @param val     the val
     * @param timeout the timeout
     */
    public void lock(String key, String val, long timeout) {
        VariableLock vl = getVariableLock(timeout, key);
        variableLockServer.lock(vl, val);
    }

    /**
     * 加锁.
     *
     * <p>key和val的数量需要一致</p>
     *
     * @param key     the key
     * @param val     the val
     * @param timeout the timeout
     */
    public void lock(String[] key, String[] val, long timeout) {
        VariableLock vl = getVariableLock(timeout, key);
        variableLockServer.lock(vl, val);
    }

    /**
     * 解锁.
     *
     * @param key the key
     * @param val the val
     */
    public void unlock(String key, String val) {
        VariableLock vl = getVariableLock(key);
        variableLockServer.unlock(vl, val);
    }

    /**
     * 解锁.
     *
     * <p>key和val的数量需要一致</p>
     *
     * @param key the key
     * @param val the val
     */
    public void unlock(String[] key, String[] val) {
        VariableLock vl = getVariableLock(key);
        variableLockServer.unlock(vl, val);
    }


    private static VariableLock getVariableLock(String... names) {
        return getVariableLock(60 * 1000, names);
    }

    private static VariableLock getVariableLock(long timeout, String... names) {
        return new VariableLock() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return VariableLock.class;
            }

            @Override
            public String[] value() {
                return names;
            }

            @Override
            public Origin[] origin() {
                return new Origin[0];
            }

            @Override
            public boolean spin() {
                return false;
            }

            @Override
            public boolean lock() {
                return true;
            }

            @Override
            public boolean unlock() {
                return true;
            }

            @Override
            public long timeout() {
                return timeout;
            }

            @Override
            public String message() {
                return "处理中";
            }
        };
    }
}
