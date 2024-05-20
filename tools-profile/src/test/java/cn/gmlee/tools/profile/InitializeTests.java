package cn.gmlee.tools.profile;

import cn.gmlee.tools.base.builder.KvBuilder;
import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.profile.conf.ProfileProperties;
import cn.gmlee.tools.profile.initializer.OracleProfileDataInitializer;
import cn.gmlee.tools.profile.initializer.ProfileDataTemplate;
import oracle.jdbc.pool.OracleDataSource;

import java.sql.SQLException;

/**
 * The type Initialize tests.
 */
public class InitializeTests {

    /**
     * 初始化数据库.
     * <p>
     * 给所有表增加env字段
     * </p>
     *
     * @param args the input arguments
     */
    public static void main(String[] args) throws SQLException {
        // 注意环境地址
        String url = "jdbc:oracle:thin:@//scan-ip.exgrain.org:1521/ygwdb";
        // 开始批量处理
        handleBatch(url, KvBuilder.array("carry", "carry123"));
    }

    private static void handleBatch(String url, Kv<String, String>... kvs) throws SQLException {
        for (Kv<String, String> kv : kvs) {
            System.out.println(String.format("%s::::::::::%s", kv.getKey(), kv.getKey()));
            System.out.println("---------- Start. ---------- ");
            ExceptionUtil.sandbox(() -> handleOnce(url, kv));
            System.out.println("---------- Done. ---------- ");
        }
    }

    private static void handleOnce(String url, Kv<String, String> kv) throws SQLException {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL(url);
        dataSource.setUser(kv.getKey());
        dataSource.setPassword(kv.getVal());
        ProfileProperties properties = new ProfileProperties();
        ProfileDataTemplate profileDataTemplate = new ProfileDataTemplate(dataSource, properties);
        profileDataTemplate.init(new OracleProfileDataInitializer());
    }
}
