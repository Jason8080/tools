package cn.gmlee.tools.profile;

import cn.gmlee.tools.profile.conf.ProfileProperties;
import cn.gmlee.tools.profile.initializer.OracleProfileDataInitializer;
import cn.gmlee.tools.profile.initializer.ProfileDataTemplate;
import oracle.jdbc.pool.OracleDataSource;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.SQLException;

/**
 * The type Initialize tests.
 */
public class InitializeTests {

    /**
     * 初始化数据库.
     * <p>
     *     给所有表增加env字段
     * </p>
     *
     * @param args the input arguments
     */
    public static void main(String[] args) throws SQLException {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL("jdbc:oracle:thin:@//scan-ip.exgrain.org:1521/ygwdb");
        dataSource.setUser("cpp");
        dataSource.setPassword("cpp1234");
        ProfileProperties properties = new ProfileProperties();
        ProfileDataTemplate profileDataTemplate = new ProfileDataTemplate(dataSource, properties);
        profileDataTemplate.init(new OracleProfileDataInitializer());
    }
}
