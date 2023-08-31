package cn.gmlee.tools.gray.server;

import org.springframework.web.server.ServerWebExchange;

/**
 * 用户处理器.
 */
public class UserHandler extends AbstractGrayHandler {

    /**
     * Instantiates a new Abstract gray handler.
     *
     * @param grayServer the gray server
     */
    protected UserHandler(GrayServer grayServer) {
        super(grayServer);
    }

    @Override
    public String name() {
        return "user";
    }

    @Override
    public boolean allow(ServerWebExchange exchange) {
        return false;
    }
}
