package cn.gmlee.tools.gray.server;

import org.springframework.web.server.ServerWebExchange;

/**
 * 权重处理器.
 */
public class WeightHandler implements GrayHandler {

    @Override
    public String name() {
        return "weight";
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
