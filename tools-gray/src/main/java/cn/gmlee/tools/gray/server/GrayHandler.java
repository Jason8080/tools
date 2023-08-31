package cn.gmlee.tools.gray.server;

import org.springframework.web.server.ServerWebExchange;

/**
 * 网关灰度请求处理器.
 */
public interface GrayHandler {
    /**
     * 处理器名称.
     *
     * @return the string
     */
    String name();

    /**
     * 是否支持并处理.
     *
     * @param exchange the exchange
     * @return the boolean
     */
    boolean support(ServerWebExchange exchange);


    /**
     * 是否允许并进入灰度节点.
     *
     * @param exchange the exchange
     * @return the boolean
     */
    boolean allow(ServerWebExchange exchange);
}
