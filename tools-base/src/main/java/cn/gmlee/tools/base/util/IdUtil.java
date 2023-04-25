package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.alg.Snowflake;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * The type Id util.
 *
 * @author Jas °
 */
public class IdUtil {
    private static final SecureRandom random = new SecureRandom();
    private static final Snowflake snowflake = new Snowflake();

    /**
     * 生成雪花ID.
     * <p>
     * 采用MAC + PID生成唯一机器编号.
     * (采用该方案的还有: mybatis-plus).
     * </p>
     *
     * @return the long
     */
    public static long snowflakeId() {
        return snowflake.next();
    }

    /**
     * 生成雪花ID.
     * <p>
     * 自定义唯一机器编号.
     * (人工保证部署的机器节点编号唯一)
     * </p>
     *
     * @param datacenterId the datacenter id
     * @param workerId     the worker id
     * @return the long
     */
    public static long snowflakeId(long datacenterId, long workerId) {
        return new Snowflake(datacenterId, workerId).next();
    }

    /**
     * 原生UUID.
     *
     * @return the string
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 去`-`号的UUID.
     *
     * @return the string
     */
    public static String uuidReplace() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 取出指定长度的UUID.
     *
     * @param length the length
     * @return string string
     */
    public static String uuidReplace(int length) {
        StringBuilder sb = new StringBuilder(uuidReplace());
        while (sb.length() < length) {
            sb.append(uuidReplace());
        }
        return sb.substring(0, length);
    }

    /**
     * 大写的UUID.
     *
     * @return the string
     */
    public static String uuidReplaceUpperCase() {
        return uuidReplace().toUpperCase();
    }

    /**
     * 生成指定范围的随机数.
     *
     * @param min the min
     * @param max the max
     * @return the int
     */
    public static int randomInteger(int min, int max) {
        return random.nextInt(max) % (max - min + 1) + min;
    }

    /**
     * 生成指随机数.
     *
     * @return the long
     */
    public static long randomLong() {
        return random.nextLong();
    }

    /**
     * 生成指定长度的随机数.
     *
     * @param length the length
     * @return the string
     */
    public static String randomNumber(int length) {
        StringBuilder sb = new StringBuilder();
        while (length-- > 0) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
