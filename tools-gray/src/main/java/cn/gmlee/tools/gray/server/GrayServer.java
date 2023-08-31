package cn.gmlee.tools.gray.server;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.gray.assist.ExchangeAssist;
import cn.gmlee.tools.gray.conf.GrayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.List;

/**
 * 灰度服务.
 */
@Slf4j
public class GrayServer {

    @Autowired
    private List<GrayHandler> handlers = Collections.emptyList();

    /**
     * 灰度配置.
     */
    public final GrayProperties properties;

    /**
     * Instantiates a new Gray server.
     *
     * @param properties the properties
     */
    public GrayServer(GrayProperties properties) {
        this.properties = properties;
    }

    /**
     * 灰度检查.
     *
     * @param exchange the exchange
     * @return the boolean
     */
    public boolean check(ServerWebExchange exchange) {
        for (GrayHandler handler : handlers){
            // 不支持不处理
            if(!handler.support(exchange)){
                continue;
            }
            // 是否拒绝灰度
            if(!handler.allow(exchange)){
                log.info("--------- 处理器 {} 拒绝灰度 ---------", handler.name());
                return false;
            }
        }
        return true;
//
//        // 获取请求令牌
//        List<String> tokens = getTokens(exchange);
//        if (BoolUtil.isEmpty(tokens)) {
//            // 没有令牌默认不进入灰度
//            return false;
//        }
//        // 获取请求令牌
//        String token = tokens.get(0);
//        return check(token);
    }

    private List<String> getTokens(ServerWebExchange exchange) {
        HttpHeaders headers = ExchangeAssist.getHeaders(exchange);
        List<String> tokens = headers.get(properties.getToken());
        if (BoolUtil.notEmpty(tokens)) {
            return tokens;
        }
        String name = headers.containsKey(properties.getToken().toLowerCase()) ?
                properties.getToken() : properties.getToken().toUpperCase();
        return headers.get(name);
    }

    private boolean check(String token) {
        // 没有令牌默认不进入灰度
        if (BoolUtil.isEmpty(token)) {
            return false;
        }
        // TODO: 继续Redis..
        return true;
    }
}
