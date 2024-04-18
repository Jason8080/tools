package cn.gmlee.tools.cache2.server.ds.mysql;

import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.enums.DataType;
import cn.gmlee.tools.cache2.kit.ClassKit;
import cn.gmlee.tools.cache2.kit.ElKit;
import cn.gmlee.tools.cache2.server.ds.AbstractDsServer;
import cn.gmlee.tools.cache2.server.ds.dao.mapper.SqlMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 字典缓存.
 */
@Data
public class SqlServer extends AbstractDsServer implements DbServer {

    @Lazy
    @Autowired
    private SqlMapper sqlMapper;

    @Override
    public boolean support(Cache cache) {
        return DataType.SQL.equals(cache.dataType());
    }

    @Override
    protected List<Map<String, Object>> list(Cache cache, Object result, Field field) {
        // 解析查询条件
        Map<String, Object> map = ClassKit.generateMapUseCache(result);
        String where = ElKit.parse(cache.where(), map);
        return sqlMapper.list(cache.target(), where);
    }
}
