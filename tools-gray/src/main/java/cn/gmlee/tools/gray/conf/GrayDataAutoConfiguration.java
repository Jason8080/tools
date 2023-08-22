package cn.gmlee.tools.gray.conf;

import cn.gmlee.tools.gray.initializer.GrayDataInitializer;
import cn.gmlee.tools.gray.initializer.GrayDataInitializerTemplate;
import cn.gmlee.tools.gray.initializer.MysqlGrayDataInitializer;
import cn.gmlee.tools.gray.initializer.OracleGrayDataInitializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * 数据灰度自动装配类.
 */
@EnableConfigurationProperties(GrayProperties.class)
public class GrayDataAutoConfiguration {

    private final DataSource dataSource;

    /**
     * Instantiates a new Gray data auto configuration.
     *
     * @param dataSource the data source
     */
    public GrayDataAutoConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Mysql gray data initializer mysql gray data initializer.
     *
     * @return the mysql gray data initializer
     */
    @Bean
    @ConditionalOnMissingBean(MysqlGrayDataInitializer.class)
    public MysqlGrayDataInitializer mysqlGrayDataInitializer(){
        return new MysqlGrayDataInitializer();
    }

    /**
     * Oracle gray data initializer oracle gray data initializer.
     *
     * @return the oracle gray data initializer
     */
    @Bean
    @ConditionalOnMissingBean(OracleGrayDataInitializer.class)
    public OracleGrayDataInitializer oracleGrayDataInitializer(){
        return new OracleGrayDataInitializer();
    }

    /**
     * 灰度初始化模版.
     *
     * @param initializers the initializers
     * @param properties   the properties
     * @return the gray data initializer template
     * @throws SQLException the sql exception
     */
    @Bean
    @ConditionalOnBean(GrayDataInitializer.class)
    public GrayDataInitializerTemplate grayDataInitializerTemplate(List<GrayDataInitializer> initializers, GrayProperties properties) throws SQLException {
        GrayDataInitializerTemplate template = new GrayDataInitializerTemplate(dataSource, properties);
        template.init(initializers.toArray(new GrayDataInitializer[0]));
        return template;
    }
}
