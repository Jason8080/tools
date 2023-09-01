package cn.gmlee.tools.gray.server;

/**
 * 权重处理器.
 */
public class WeightHandler extends AbstractGrayHandler {
    /**
     * Instantiates a new Abstract gray handler.
     *
     * @param grayServer the gray server
     */
    protected WeightHandler(GrayServer grayServer) {
        super(grayServer);
    }

    @Override
    public String name() {
        return weight;
    }

    @Override
    public boolean allow(String num) {
        return false;
    }
}
