package cn.gmlee.tools.profile.initializer;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.CollectionUtil;
import cn.gmlee.tools.profile.conf.ProfileProperties;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 数据隔离初始化模版.
 */
@Slf4j
public class ProfileDataTemplate {

    private final DataSource dataSource;
    private final ProfileProperties properties;

    private String databaseProductName;

    /**
     * Instantiates a new Gray data initializer template.
     *
     * @param dataSource the data source
     * @param properties the gray properties
     */
    public ProfileDataTemplate(DataSource dataSource, ProfileProperties properties) {
        this.dataSource = dataSource;
        this.properties = properties;
    }

    /**
     * Init.
     *
     * @param initializers the initializers
     * @throws SQLException the sql exception
     */
    public void init(ProfileDataInitializer... initializers) throws SQLException {
        AssertUtil.notEmpty(initializers, "灰度初始化器丢失");
        // 获取数据库型号
        String database = getDatabaseProductName();
        // 适配数据库操作
        for (ProfileDataInitializer initializer : initializers) {
            if (!initializer.support(database)) {
                continue;
            }
            // 初始化连接
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            try {
                // 查询所有表和字段信息
                Map<String, Map<String, Object>> columns = initializer.getColumn(conn, properties);
                // 过滤缺失环境字段的表
                CollectionUtil.filter(columns, (String k, Map<String, Object> v) -> !v.containsKey(properties.getField()));
                // 追加环境字段到缺失表
                for (Map.Entry<String, Map<String, Object>> entry : columns.entrySet()) {
                    try {
                        initializer.addColumn(properties, conn, entry.getKey(), entry.getValue());
                        log.debug("数据隔离库表{}初始化成功 {}:{}", entry.getKey(), entry.getValue());
                    } catch (Exception e) {
                        log.error("数据隔离库表{}初始化失败", entry.getKey(), e);
                    }
                }
            } finally {
                // 提交请求
                conn.commit();
                // 关闭连接
                conn.close();
            }
        }
    }

    public String getDatabaseProductName() throws SQLException {
        if (BoolUtil.notEmpty(this.databaseProductName)) {
            return this.databaseProductName;
        }
        return this.databaseProductName = dataSource.getConnection().getMetaData().getDatabaseProductName();
    }
}
