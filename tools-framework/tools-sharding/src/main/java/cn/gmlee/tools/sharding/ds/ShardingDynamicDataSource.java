package cn.gmlee.tools.sharding.ds;

import cn.gmlee.tools.ds.dynamic.DynamicDataSource;
import lombok.Getter;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 动态数据源.
 */
public class ShardingDynamicDataSource extends DynamicDataSource {
    private final String oldName = "ToolsShardingSphereDataSource-0";
    private final String newName = "ToolsShardingSphereDataSource-1";
    private final Map<Object, Object> targetDataSource;
    @Getter
    private volatile String dataSourceName = oldName;

    /**
     * Instantiates a new Sharding dynamic data source.
     *
     * @param targetDataSource the target data source
     */
    public ShardingDynamicDataSource(Map<Object, Object> targetDataSource) {
        this.targetDataSource = targetDataSource;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return dataSourceName;
    }

    /**
     * 切换数据源.
     *
     * @param dataSource the data source
     */
    public void cutDataSource(DataSource dataSource){
        // 保存当前值
        String current = this.dataSourceName;
        // 判断新名称
        String dataSourceName = targetDataSource.containsKey(newName) ? oldName : newName;
        // 添加新对象
        targetDataSource.put(dataSourceName, dataSource);
        super.afterPropertiesSet();
        // 切换新对象
        this.dataSourceName = dataSourceName;
        // 删除旧对象
        targetDataSource.remove(current);
    }
}
