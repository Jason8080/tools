package cn.gmlee.tools.gray.server;

/**
 * 用户处理器.
 */
public class UserHandler extends AbstractGrayHandler {
    /**
     * Instantiates a new Abstract gray handler.
     *
     * @param grayServer the gray server
     */
    public UserHandler(GrayServer grayServer) {
        super(grayServer);
    }

    @Override
    public String name() {
        return user;
    }

    @Override
    public boolean allow(String app, String token) {
        return false;
    }
}
