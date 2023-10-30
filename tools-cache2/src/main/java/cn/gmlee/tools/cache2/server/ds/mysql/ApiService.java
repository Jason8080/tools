package cn.gmlee.tools.cache2.server.ds.mysql;

import cn.gmlee.tools.base.util.ClassUtil;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.enums.DataType;
import cn.gmlee.tools.cache2.kit.ElKit;
import cn.gmlee.tools.cache2.server.ds.AbstractDsServer;
import lombok.Data;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 字典缓存.
 */
@Data
public class ApiService extends AbstractDsServer {

    private RestTemplate restTemplate;

    @Override
    public boolean support(Cache cache) {
        return DataType.API.equals(cache.dataType());
    }

    @Override
    protected List<Map<String, Object>> list(Cache cache, Object result, Field field) {
        // 解析查询条件
        Map<String, Object> map = ClassUtil.generateMapUseCache(result);
        String where = ElKit.parse(cache.where(), map);
        Map<String, Object> params = WebUtil.getParams(where);
        return restTemplate.getForObject(cache.table(), List.class, params);
    }
}
