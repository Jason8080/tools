package cn.gmlee.tools.cache2.server.ds.http;

import cn.gmlee.tools.base.mod.HttpResult;
import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.base.util.ClassUtil;
import cn.gmlee.tools.base.util.HttpUtil;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.enums.DataType;
import cn.gmlee.tools.cache2.kit.ElKit;
import cn.gmlee.tools.cache2.server.ds.AbstractDsServer;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 字典缓存.
 */
@Data
public class ApiServer extends AbstractDsServer implements HttpServer {

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
        String url = WebUtil.addParam(cache.target(), params);
        // 设置请求头部
//        Map<String, String> headers = WebUtil.getCurrentHeaderMap();
//        Kv<String, String>[] kvs = KvBuilder.array(headers);
        HttpResult httpResult = HttpUtil.get(url/*, kvs*/);
        R r = httpResult.jsonResponseBody2bean(R.class);
        return (List<Map<String, Object>>) r.getData();
    }
}
