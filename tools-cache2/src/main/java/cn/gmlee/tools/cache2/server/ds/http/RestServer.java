package cn.gmlee.tools.cache2.server.ds.http;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.enums.DataType;
import cn.gmlee.tools.cache2.kit.ClassKit;
import cn.gmlee.tools.cache2.kit.ElKit;
import cn.gmlee.tools.cache2.server.ds.AbstractDsServer;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 字典缓存.
 */
@Data
public class RestServer extends AbstractDsServer implements HttpServer {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public boolean support(Cache cache) {
        return DataType.API.equals(cache.dataType());
    }

    @Override
    protected List<Map<String, Object>> list(Cache cache, Object result, Field field) {
        // 解析查询条件
        Map<String, Object> map = ClassKit.generateMapUseCache(result);
        String where = ElKit.parse(cache.where(), map);
        Map<String, Object> params = WebUtil.getParams(where);
        String url = WebUtil.addParam(cache.target(), params);
        // 设置请求头部
        Map<String, String> headers = WebUtil.getCurrentHeaderMap();
        restTemplate.headForHeaders(url, headers);
        R r = restTemplate.getForObject(url, R.class);
        return (List<Map<String, Object>>) r.getData();
    }
}
