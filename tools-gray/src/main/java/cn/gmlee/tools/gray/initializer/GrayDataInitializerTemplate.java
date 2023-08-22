package cn.gmlee.tools.gray.initializer;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.CollectionUtil;
import cn.gmlee.tools.gray.conf.GrayProperties;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 灰度数据初始化模版.
 */
@Slf4j
public class GrayDataInitializerTemplate {

    /**
     * The Data source.
     */
    private final DataSource dataSource;
    private final GrayProperties properties;

    /**
     * Instantiates a new Gray data initializer template.
     *
     * @param dataSource the data source
     * @param properties the gray properties
     */
    public GrayDataInitializerTemplate(DataSource dataSource, GrayProperties properties) {
        this.dataSource = dataSource;
        this.properties = properties;
    }

    /**
     * Init.
     *
     * @param initializers the initializers
     * @throws SQLException the sql exception
     */
    public void init(GrayDataInitializer... initializers) throws SQLException {
        AssertUtil.notEmpty(initializers, "灰度初始化器丢失");
        // 获取数据库型号
        String database = dataSource.getConnection().getMetaData().getDatabaseProductName();
        // 适配数据库操作
        for (GrayDataInitializer initializer : initializers) {
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
                CollectionUtil.filter(columns, (String k, Map<String, Object> v) -> !v.containsKey(properties.getEvn()));
                // 追加环境字段到缺失表
                for (Map.Entry<String, Map<String, Object>> entry : columns.entrySet()){
                    try {
                        initializer.addColumn(properties, conn, entry.getKey(), entry.getValue());
                    } catch (Exception e) {
                        log.error("灰度数据库表{}初始化失败", entry.getKey(), e);
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
}
