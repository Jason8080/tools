package cn.gmlee.tools.gray.assist;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.gray.server.GrayHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Exchange assist.
 */
@Slf4j
public class ExchangeAssist {

    /**
     * Filter boolean.
     *
     * @param exchange the exchange
     * @return the boolean
     */
    public static boolean filter(ServerWebExchange exchange) {
        URI uri = exchange.getRequest().getURI();
        String scheme = uri.getScheme();
        return scheme.equalsIgnoreCase("lb");
    }

    /**
     * Gets headers.
     *
     * @param exchange the exchange
     * @return the headers
     */
    public static HttpHeaders getHeaders(Object exchange) {
        if (exchange instanceof ServerWebExchange) {
            return getHeaders((ServerWebExchange) exchange);
        }
        if (exchange instanceof RequestDataContext) {
            return getHeaders((RequestDataContext) exchange);
        }
        log.error("灰度负载均衡器无法解析请求: exchange:: {}", exchange.getClass());
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    /**
     * Gets headers.
     *
     * @param exchange the exchange
     * @return the headers
     */
    public static HttpHeaders getHeaders(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders();
    }

    /**
     * Gets headers.
     *
     * @param exchange the exchange
     * @return the headers
     */
    public static HttpHeaders getHeaders(RequestDataContext exchange) {
        return exchange.getClientRequest().getHeaders();
    }

    /**
     * Gets service id.
     *
     * @param exchange the exchange
     * @return the service id
     */
    public static String getServiceId(Object exchange) {
        if (exchange instanceof ServerWebExchange) {
            return getServiceId((ServerWebExchange) exchange);
        }
        if (exchange instanceof RequestDataContext) {
            return getServiceId((RequestDataContext) exchange);
        }
        log.error("灰度负载均衡器无法解析请求: exchange:: {}", exchange.getClass());
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    /**
     * Gets service id.
     *
     * @param exchange the exchange
     * @return the service id
     */
    public static String getServiceId(ServerWebExchange exchange) {
        URI requestUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        return requestUri.getHost();
    }

    /**
     * Gets service id.
     *
     * @param exchange the exchange
     * @return the service id
     */
    public static String getServiceId(RequestDataContext exchange) {
        return exchange.getClientRequest().getUrl().getHost();
    }

    /**
     * Gets tokens.
     *
     * @param exchange the exchange
     * @param token    the token
     * @return the tokens
     */
    public static Map<String, String> getTokens(ServerWebExchange exchange, String token) {
        HttpHeaders headers = getHeaders(exchange);
        return getTokens(headers, token);
    }

    /**
     * Gets tokens.
     *
     * @param headers the headers
     * @param token   the token
     * @return the tokens
     */
    public static Map<String, String> getTokens(HttpHeaders headers, String token) {
        Map<String, String> tokens = new HashMap<>(4);
        tokens.put(GrayHandler.ip, getToken(headers, WebUtil.REMOTE_ADDRESS_HOST));
        tokens.put(GrayHandler.user, getToken(headers, token));
        // 每次消耗1张令牌
        tokens.put(GrayHandler.weight, "1");
        tokens.put(GrayHandler.custom, getToken(headers, token));
        return tokens;
    }

    private static String getToken(HttpHeaders headers, String head) {
        List<String> heads = headers.get(head);
        if (BoolUtil.isEmpty(heads)) {
            return null;
        }
        return heads.get(0);
    }

    /**
     * Add header.
     *
     * @param exchange the exchange
     * @param name     the name
     * @param value    the value
     */
    public static void addHeader(Object exchange, String name, String value) {
        if (exchange instanceof ServerWebExchange) {
            addHeader((ServerWebExchange) exchange, name, value);
            return;
        }
        if (exchange instanceof RequestDataContext) {
            addHeader((RequestDataContext) exchange, name, value);
            return;
        }
        log.error("灰度负载均衡器无法解析请求: exchange:: {}", exchange.getClass());
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    /**
     * Add header.
     *
     * @param exchange the exchange
     * @param name     the name
     * @param value    the value
     */
    public static void addHeader(ServerWebExchange exchange, String name, String value) {
        exchange.getRequest().mutate().header(name, value).build();
    }

    /**
     * Add header.
     *
     * @param exchange the exchange
     * @param name     the name
     * @param value    the value
     */
    public static void addHeader(RequestDataContext exchange, String name, String value) {
        exchange.getClientRequest().getHeaders().add(name, value);
    }
}
