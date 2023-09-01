package cn.gmlee.tools.gray.server;

/**
 * 定制处理器.
 */
public class CustomHandler extends AbstractGrayHandler {
    /**
     * Instantiates a new Abstract gray handler.
     *
     * @param grayServer the gray server
     */
    public CustomHandler(GrayServer grayServer) {
        super(grayServer);
    }

    @Override
    public String name() {
        return custom;
    }

    @Override
    public boolean allow(String token) {
        return false;
    }
}
