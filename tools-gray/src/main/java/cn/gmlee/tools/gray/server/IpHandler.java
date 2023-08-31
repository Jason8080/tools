package cn.gmlee.tools.gray.server;

import org.springframework.web.server.ServerWebExchange;

/**
 * 地址处理器.
 */
public class IpHandler implements GrayHandler {

    @Override
    public String name() {
        return "ip";
    }

    @Override
    public boolean support(ServerWebExchange exchange) {
        return false;
    }

    @Override
    public boolean allow(ServerWebExchange exchange) {
        return false;
    }
}
