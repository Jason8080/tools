package cn.gmlee.tools.gray.assist;

import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * The type Exchange assist.
 */
public class ExchangeAssist {

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
     * Gets service id.
     *
     * @param exchange the exchange
     * @return the service id
     */
    public static String getServiceId(ServerWebExchange exchange) {
        URI requestUri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        return requestUri.getHost();
    }
}
