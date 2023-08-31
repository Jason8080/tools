package cn.gmlee.tools.gray.server;

import org.springframework.web.server.ServerWebExchange;

/**
 * 定制处理器.
 */
public class CustomHandler implements GrayHandler {

    @Override
    public String name() {
        return "custom";
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
