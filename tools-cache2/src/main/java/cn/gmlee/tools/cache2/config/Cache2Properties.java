package cn.gmlee.tools.cache2.config;

import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.cache2.enums.DataType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cache2参数
 */
@Data
@ConfigurationProperties(prefix = "tools.cache2")
public class Cache2Properties implements Cache2Conf {
    // ------------------------------------
    public boolean log = false;
    public int depth = Int.THREE;
    // ------------------------------------
    public DataType dataType = DataType.SQL;
    public Boolean enable = true;
    public Long expire = 24 * 3600L;
    // ------------------------------------
    public String table;
    public String key;
    public String get;
    public String put;
    public String where;
    // ------------------------------------
}
