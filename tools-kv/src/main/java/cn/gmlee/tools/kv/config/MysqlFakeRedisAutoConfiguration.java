package cn.gmlee.tools.kv.config;

import cn.gmlee.tools.base.util.*;
import cn.gmlee.tools.kv.mysql.redis.FakeRedis;
import cn.gmlee.tools.kv.cmd.MysqlFakeRedisConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.net.InetAddress;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * 伪Redis自动装载配置类.
 *
 * @author Jas°
 * @date 2021/7/20 (周二)
 */
@EnableScheduling
public class MysqlFakeRedisAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MysqlFakeRedisAutoConfiguration.class);

    private static final String SCHEDULED_RECYCLE = "fake:redis:lock:scheduled_recycle";

    private DataSource dataSource;

    private FakeRedis fakeRedis;

    public MysqlFakeRedisAutoConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        try {
            List<Map<String, Object>> exist = JdbcUtil.exec(dataSource, MysqlFakeRedisConstant.EXIST_TABLE_SQL);
            if (BoolUtil.isEmpty(exist)) {
                JdbcUtil.exec(dataSource, MysqlFakeRedisConstant.CREATE_TABLE_SQL);
            }
        } catch (Exception e) {
            ExceptionUtil.cast(String.format("初始化失败"), e);
        }
    }

    @Bean
    public FakeRedis fakeRedis() {
        if (fakeRedis != null) {
            return fakeRedis;
        }
        return fakeRedis = new FakeRedis(dataSource);
    }

    @Scheduled(cron = "0 0 4 * * ?")
    public void recycle() throws Exception {
        if (lock()) {
            logger.debug("{}: 占锁成功即将执行回收任务..", InetAddress.getLocalHost());
            JdbcUtil.exec(dataSource, MysqlFakeRedisConstant.RECYCLE_KV_SQL);
        }
    }

    private Boolean lock() {
        String ms = TimeUtil.getCurrentMs();
        return fakeRedis.setNx(SCHEDULED_RECYCLE, ms, LocalDateTimeUtil.offsetCurrent(6, ChronoUnit.SECONDS));
    }
}
