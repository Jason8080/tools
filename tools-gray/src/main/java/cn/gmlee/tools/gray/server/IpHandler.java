package cn.gmlee.tools.gray.server;

/**
 * 地址处理器.
 */
public class IpHandler extends AbstractGrayHandler {
    /**
     * Instantiates a new Abstract gray handler.
     *
     * @param grayServer the gray server
     */
    public IpHandler(GrayServer grayServer) {
        super(grayServer);
    }

    @Override
    public String name() {
        return ip;
    }

    @Override
    public boolean allow(String ip) {
        return false;
    }
}
