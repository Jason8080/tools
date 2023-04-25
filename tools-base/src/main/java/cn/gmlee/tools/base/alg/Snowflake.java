package cn.gmlee.tools.base.alg;

import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 雪花算法.
 * @see cn.gmlee.tools.base.alg.snowflake.Snowflake
 * <p>
 * 该算法可以使用雪花可用69.73年
 * ==========================================================
 * 最高位为0存储符号: 保持生成的编号为自然数(即大于0的数值)
 * 采用41位存储时间: 2^41 = 2199023255552
 * 采用10位存储地址: 2^10 = 1024 (表示最多可以部署1024个NODE)
 * 采用12位存储编号: 2^12 = 4096 (每毫秒最多可生成4096个ID)
 * 1年 = 31536000秒 = 31536000000毫秒
 * ==========================================================
 * </p>
 *
 * @author Jas °
 * @return the long
 */
@Deprecated
public class Snowflake {

    private static final Logger logger = LoggerFactory.getLogger(Snowflake.class);
    /**
     * 时间起始标记点，作为基准，一般取系统的最近时间（一旦确定不能变动）
     */
    private final long activeTime = 1288834974657L;
    /**
     * 机器标识位数
     */
    private final long workerIdBits = 5L;
    /**
     * 数据中心(机房)标识位数
     */
    private final long datacenterIdBits = 5L;
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    /**
     * 上次生产 ID 时间戳
     */
    private long lastTimestamp = -1L;
    /**
     * 并发控制
     */
    private long id = 0L;
    /**
     * 毫秒内自增位
     */
    private final long sequenceBits = 12L;
    ;
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    /**
     * 时间戳左移动位
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private final long workerId;
    /**
     * 数据标识 ID 部分
     */
    private final long datacenterId;

    /**
     * 无参构造器.
     */
    public Snowflake() {
        this.datacenterId = getDatacenterId(maxDatacenterId);
        this.workerId = getMaxWorkerId(datacenterId, maxWorkerId);
    }

    /**
     * 有参构造器
     *
     * @param datacenterId 数据中心 ID
     * @param workerId     工作机器 ID
     */
    public Snowflake(long datacenterId, long workerId) {
        AssertUtil.isTrue(workerId < maxWorkerId && workerId > 0, String.format("机器编号超出范围: 0~%s", maxWorkerId));
        AssertUtil.isTrue(datacenterId < maxDatacenterId && datacenterId > 0, String.format("机房编号超出范围", maxDatacenterId));
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }


    /**
     * 获取下一个 ID
     *
     * @return 下一个 ID
     */
    public synchronized long next() {
        long timestamp = System.currentTimeMillis();
        //闰秒
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= Int.FIVE) {
                try {
                    wait(offset << 1);
                    timestamp = System.currentTimeMillis();
                    if (timestamp < lastTimestamp) {
                        ExceptionUtil.cast(XCode.SERVER_TIME5001.code, String.format("你见过逆时针的时钟吗? 所以%s时间内无法生成", offset));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                ExceptionUtil.cast(XCode.SERVER_TIME5001.code, String.format("你见过逆时针的时钟吗? 所以%s时间内无法生成", offset));
            }
        }

        if (lastTimestamp == timestamp) {
            // 相同毫秒内，序列号自增
            id = (id + 1) & sequenceMask;
            if (id == 0) {
                // 同一毫秒的序列数已经达到最大
                timestamp = nextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒内，序列号置为 1 - 3 随机数
            id = ThreadLocalRandom.current().nextLong(1, 3);
        }

        lastTimestamp = timestamp;

        // 时间戳部分 | 数据中心部分 | 机器标识部分 | 序列号部分
        return ((timestamp - activeTime) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | id;
    }

    private long nextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    private long getDatacenterId(long maxDatacenterId) {
        long id = 0L;
        try {
            //获取本机(或者服务器ip地址)
            //DESKTOP-123DDS/192.168.1.87
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            //一般不是null会进入else
            if (network == null) {
                id = 1L;
            } else {
                //获取物理网卡地址
                byte[] mac = network.getHardwareAddress();
                if (null != mac) {
                    id = ((0x000000FF & (long) mac[mac.length - 2]) | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
                    id = id % (maxDatacenterId + 1);
                }
            }
        } catch (Exception e) {
            logger.warn("get datacenter id exception: " + e.getMessage());
        }
        return id;
    }

    private long getMaxWorkerId(long datacenterId, long maxWorkerId) {
        StringBuilder macPid = new StringBuilder();
        macPid.append(datacenterId);
        //获取jvm进程信息
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (BoolUtil.notEmpty(name)) {
            /*
             * 获取进程PID
             */
            macPid.append(name.split("@")[0]);
        }
        /*
         * MAC + PID 的 hashcode 获取16个低位
         */
        return (macPid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }
}
