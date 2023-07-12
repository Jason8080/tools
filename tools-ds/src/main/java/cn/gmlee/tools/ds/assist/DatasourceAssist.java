package cn.gmlee.tools.ds.assist;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.ds.dynamic.DynamicDataSource;
import cn.gmlee.tools.ds.dynamic.DynamicDataSourceHolder;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用数据源辅助工具
 *
 * @author Jas °
 * @date 2020 /11/25 (周三)
 */
public class DatasourceAssist {
    /**
     * Gets datasource type.
     *
     * @param <T>                  the type parameter
     * @param dataSourceProperties the data source properties
     * @return the datasource type
     */
    public static <T extends DataSource> Class<T> getDatasourceType(Map<String, String> dataSourceProperties) {
        String type = dataSourceProperties.get("type");
        String className = StringUtils.isEmpty(type) ? "com.alibaba.druid.pool.DruidDataSource" : type;
        try {
            Class<?> aClass = Class.forName(className);
            return (Class<T>) aClass;
        } catch (ClassNotFoundException e) {
            throw new SkillException(XCode.THIRD_PARTY_FAIL.code, "Data source cannot load..", e);
        }
    }


    /**
     * 创建动态数据源.
     * <p>
     * 不能创建多个, 因为 DynamicDataSourceHolder.addDatasourceKeys(name); 是共用的
     * </p>
     *
     * @param dsMap         the ds map
     * @param datasourceMap the datasource map
     * @return the dynamic data source
     */
    public static DynamicDataSource createDynamicDataSource(Map<String, Map<String, String>> dsMap, Map<String, String> datasourceMap) {
        return createDynamicDataSource(dsMap, datasourceMap, DynamicDataSource.class);
    }


    /**
     * 创建动态数据源.
     * <p>
     * 不能创建多个, 因为 DynamicDataSourceHolder.addDatasourceKeys(name); 是共用的
     * </p>
     *
     * @param dsMap         the ds map
     * @param datasourceMap the datasource map
     * @param clazz         the clazz
     * @return the dynamic data source
     */
    public static <T extends DynamicDataSource> T createDynamicDataSource(
            Map<String, Map<String, String>> dsMap,
            Map<String, String> datasourceMap,
            Class<T> clazz
    ) {
        if (!dsMap.isEmpty()) {
            // 加载数据源
            Map<Object, Object> targetDataSource = new HashMap(dsMap.size());
            dsMap.forEach((String name, Map<String, String> map) -> {
                map.putAll(datasourceMap);
                ConfigurationPropertySource source = new MapConfigurationPropertySource(map);
                // 通过类型绑定参数并获得实例对象
                DataSource targetDatasource = new Binder(source)
                        .bind(ConfigurationPropertyName.EMPTY, Bindable.of(DatasourceAssist.getDatasourceType(map)))
                        .get();
                targetDataSource.put(name, targetDatasource);
                DynamicDataSourceHolder.addDatasourceKeys(name);
            });
            T dynamicDataSource = ExceptionUtil.suppress(() -> clazz.getConstructor().newInstance());
            dynamicDataSource.setTargetDataSources(targetDataSource);
            dynamicDataSource.afterPropertiesSet();
            return dynamicDataSource;
        }
        throw new SkillException(XCode.THIRD_PARTY_FAIL.code, "至少需要 1 个数据源");
    }


    /**
     * 创建数据源.
     *
     * @param dsMap the dsMap
     * @return the data source
     */
    public static DataSource createDataSource(Map<String, String> dsMap) {
        if (!dsMap.isEmpty()) {
            ConfigurationPropertySource source = new MapConfigurationPropertySource(dsMap);
            // 通过类型绑定参数并获得实例对象
            return new Binder(source)
                    .bind(ConfigurationPropertyName.EMPTY, Bindable.of(DatasourceAssist.getDatasourceType(dsMap)))
                    .get();
        }
        throw new SkillException(XCode.THIRD_PARTY_FAIL.code, "数据源缺少 必要的 参数");
    }
}
