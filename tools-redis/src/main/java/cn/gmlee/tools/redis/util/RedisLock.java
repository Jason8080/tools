package cn.gmlee.tools.redis.util;

import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * 分布式锁.
 * TODO: 分布式锁待优化: 拿不到锁加入队列等待执行
 *
 * @author Jas °
 * @date 2020 /8/28 (周五)
 */
public class RedisLock {

    private final Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private RedisClient redisClient;

    /**
     * Instantiates a new Redis lock.
     *
     * @param redisClient the redis client
     */
    public RedisLock(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    /**
     * 加锁.
     * <p>
     * 自旋锁: 保证每个线程都能拿到锁
     * 非公平锁: 不是每个线程都可以拿到锁
     * </p>
     *
     * @param key    the key
     * @param expire the expire
     * @return the boolean
     */
    public boolean lock(String key, long expire) {
        return lock(key, expire, expire);
    }

    /**
     * 加锁 (保存).
     * <p>
     * 自旋锁: 保证每个线程都能拿到锁
     * 非公平锁: 不是每个线程都可以拿到锁
     * </p>
     *
     * @param key    the key
     * @param val    the val
     * @param expire the expire
     * @return boolean 是否获取到锁
     */
    public boolean lock(String key, Object val, long expire) {
        return lock(key, val, expire, true);
    }


    /**
     * 加锁 (保存).
     * <p>
     * 非公平锁: 不是每个线程都可以拿到锁
     * </p>
     *
     * @param key    the key
     * @param val    the val
     * @param expire the expire
     * @param spin   the spin
     * @return the boolean
     */
    public boolean lock(String key, Object val, long expire, boolean spin) {
        if(!spin){
            return locking(key, val, expire);
        }
        // 最多自旋10000次 ≈ 30s
        AtomicInteger count = new AtomicInteger(10000);
        while (spin && count.decrementAndGet() > 0) {
            if (locking(key, val, expire)) {
                return true;
            }
            logger.info("分布式锁自旋中...");
            ExceptionUtil.sandbox(() -> sleep(Int.THREE));
        }
        return false;
    }

    private boolean locking(String key, Object val, long expire) {
        if (expire < Int.ONE) {
            // 时间太短直接失败
            return false;
        }
        AssertUtil.allNotNull(key, ExceptionUtil.sandbox(() -> String.format("键值对是空 -> %s: %s", key, val)), val);
        return redisClient.setNx(key, val, expire);
    }

    private boolean overtime(String key, Object val, long expire) {
        if (expire < Int.ONE) {
            return false;
        }
        AssertUtil.allNotNull(key, ExceptionUtil.sandbox(() -> String.format("键值对是空 -> %s: %s", key, val)), val);
        return redisClient.setEx(key, val, expire);
    }

    /**
     * 解锁 (删除).
     *
     * @param key the key
     * @param val the val
     * @return the boolean
     */
    public boolean unlock(String key, Object val) {
        AssertUtil.allNotNull(key, ExceptionUtil.sandbox(() -> String.format("键值对是空 -> %s: %s", key, val)), val);
        Object old = redisClient.get(key);
        if (BoolUtil.eq(val, old)) {
            return redisClient.delete(key);
        }
        return false;
    }

    // =================================================================================================================

    /**
     * 分布式锁 (自动续期).
     * <p>
     * 建议采用此锁, 可将过期时间设长一些, 代码执行完自动解锁。
     * 1. 如果代码运行时间超过过期时间则自动终止执行并解锁。
     * 2. 如果代码运行中宕机依靠自动过期维持业务可用性。
     * 3. 自动在代码执行超时前3ms自动续期。
     * 4. 自旋锁实现每个线程都可以拿到锁。
     * </p>
     *
     * @param key    键名
     * @param expire 键值(亦为运行时间)
     * @param run    函数(即为运行代码)
     */
    public synchronized void lock(String key, long expire, Runnable run) {
        lock(key, expire, run, 1);
    }

    /**
     * 分布式锁 (可选续期).
     * <p>
     * 建议采用此锁, 可将过期时间设长一些, 代码执行完自动解锁。
     * 1. 如果代码运行时间超过过期时间则自动终止执行并解锁。
     * 2. 如果代码运行中宕机依靠自动过期维持业务可用性。
     * 3. 非自动续期模式需要开发者考虑thread.stop()问题: 不使用全局变量。
     * 4. 自旋锁实现每个线程都可以拿到锁。
     * </p>
     *
     * @param key    键名
     * @param expire 键值(亦为运行时间)
     * @param run    函数(即为运行代码)
     * @param ot     自动续期次数
     */
    public synchronized void lock(String key, long expire, Runnable run, int ot) {
        if (expire < Int.ONE) {
            throw new RuntimeException(String.format("分布式锁使用异常: 代码运行时间过于短暂%s", expire));
        }
        FutureTask task = new FutureTask(run, null);
        Thread thread = new Thread(task);
        try {
            while (!lock(key, expire, expire, false)) {
                sleep(Int.THREE);
            }
            thread.start();
            get(key, expire, task, run, ot);
        } catch (Throwable e) {
            logger.error("分布式锁代码运行异常: 请检查代码{}", run.getClass());
            ExceptionUtil.cast(e);
        } finally {
            if (ot > -1) {
                // 终止任务
                task.cancel(true);
                // 此处采用stop(): 使用者不允许在方法内使用静态变量
                thread.stop();
            }
            // 解锁
            unlock(key, expire);
        }
    }

    // =================================================================================================================

    /**
     * 分布式锁 (自动续期).
     * <p>
     * 建议采用此锁, 可将过期时间设长一些, 代码执行完自动解锁。
     * 1. 如果代码运行时间超过过期时间则自动终止执行并解锁。
     * 2. 如果代码运行中宕机依靠自动过期维持业务可用性。
     * 3. 自动在代码执行超时前3ms自动续期。
     * 4. 自旋锁实现每个线程都可以拿到锁。
     * </p>
     *
     * @param <V>    the type parameter
     * @param key    键名
     * @param expire 键值(亦为运行时间)
     * @param call   函数(即为运行代码)
     * @return 函数返回结果. v
     */
    public synchronized <V> V lock(String key, long expire, Callable<V> call) {
        return lock(key, expire, call, 1);
    }

    /**
     * 分布式锁 (可选续期).
     * <p>
     * 建议采用此锁, 可将过期时间设长一些, 代码执行完自动解锁。
     * 1. 如果代码运行时间超过过期时间则自动终止执行并解锁。
     * 2. 如果代码运行中宕机依靠自动过期维持业务可用性。
     * 3. 非自动续期模式需要开发者考虑thread.stop()问题: 不使用全局变量。
     * 4. 自旋锁实现每个线程都可以拿到锁。
     * </p>
     *
     * @param <V>    the type parameter
     * @param key    键名
     * @param expire 键值(亦为运行时间)
     * @param call   函数(即为运行代码)
     * @param ot     自动续期次数
     * @return 函数返回结果. v
     */
    public synchronized <V> V lock(String key, long expire, Callable<V> call, int ot) {
        if (expire < Int.ONE) {
            throw new RuntimeException(String.format("分布式锁使用异常: 代码运行时间过于短暂%s", expire));
        }
        FutureTask<V> task = new FutureTask(call);
        Thread thread = new Thread(task);
        try {
            while (!lock(key, expire, expire, false)) {
                sleep(Int.THREE);
            }
            thread.start();
            return get(key, expire, task, call, ot);
        } catch (Throwable e) {
            logger.error("分布式锁代码运行异常: 请检查代码{}", call.getClass());
            return ExceptionUtil.cast(e);
        } finally {
            if (ot > -1) {
                // 终止任务
                task.cancel(true);
                // 此处采用stop(): 使用者不允许在方法内使用静态变量
                thread.stop();
            }
            // 解锁
            unlock(key, expire);
        }
    }


    // -----------------------------------------------------------------------------------------------------------------


    /**
     * 阻塞执行 (自动续期).
     */
    private void get(String key, long expire, FutureTask task, Runnable run, int ot) throws InterruptedException, java.util.concurrent.ExecutionException {
        try {
            if (ot > 0) {
                task.get(expire - Int.THREE, TimeUnit.MILLISECONDS);
            } else {
                task.get(expire, TimeUnit.MILLISECONDS);
            }
        } catch (TimeoutException e) {
            if (ot > 0) {
                if (overtime(key, expire, expire)) {
                    logger.debug(String.format("分布式锁代码运行超时: 续租成功%s", run.getClass()));
                    get(key, expire, task, run, --ot);
                }
                logger.error(String.format("分布式锁代码运行超时: 续租失败%s", run.getClass()));
            }
        }
    }

    /**
     * 阻塞执行 (自动续期).
     */
    private <V> V get(String key, long expire, FutureTask<V> task, Callable<V> call, int ot) throws InterruptedException, java.util.concurrent.ExecutionException {
        try {
            if (ot > 0) {
                return task.get(expire - Int.THREE, TimeUnit.MILLISECONDS);
            } else {
                return task.get(expire, TimeUnit.MILLISECONDS);
            }
        } catch (TimeoutException e) {
            if (ot> 0) {
                if (overtime(key, expire, expire)) {
                    logger.debug(String.format("分布式锁代码运行超时: 续租成功%s", call.getClass()));
                    return get(key, expire, task, call, --ot);
                }
                logger.error(String.format("分布式锁代码运行超时: 续租失败%s", call.getClass()));
            }
        }
        return null;
    }
}
