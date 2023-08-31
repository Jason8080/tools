package cn.gmlee.tools.gray.server;

import org.springframework.web.server.ServerWebExchange;

/**
 * 用户处理器.
 */
public class UserHandler implements GrayHandler {

    @Override
    public String name() {
        return "user";
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
