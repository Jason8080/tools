package cn.gmlee.tools.gray.server;

import org.springframework.web.server.ServerWebExchange;

/**
 * 定制处理器.
 */
public class CustomHandler extends AbstractGrayHandler {

    /**
     * Instantiates a new Abstract gray handler.
     *
     * @param grayServer the gray server
     */
    protected CustomHandler(GrayServer grayServer) {
        super(grayServer);
    }

    @Override
    public String name() {
        return "custom";
    }

    @Override
    public boolean allow(ServerWebExchange exchange) {
        return false;
    }
}
