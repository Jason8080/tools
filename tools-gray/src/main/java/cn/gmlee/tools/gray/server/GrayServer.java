package cn.gmlee.tools.gray.server;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.gray.assist.ExchangeAssist;
import cn.gmlee.tools.gray.conf.GrayProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

/**
 * The type Gray server.
 */
public class GrayServer {

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
        // 获取请求令牌
        List<String> tokens = getTokens(exchange);
        if (BoolUtil.isEmpty(tokens)) {
            // 没有令牌默认不进入灰度
            return false;
        }
        // 获取请求令牌
        String token = tokens.get(0);
        return check(token);
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
