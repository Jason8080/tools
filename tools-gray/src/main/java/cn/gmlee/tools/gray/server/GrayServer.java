package cn.gmlee.tools.gray.server;

import cn.gmlee.tools.gray.conf.GrayProperties;

import javax.servlet.ServletRequest;

/**
 * The type Gray server.
 */
public class GrayServer {

    private final GrayProperties properties;

    /**
     * Instantiates a new Gray server.
     *
     * @param properties the properties
     */
    public GrayServer(GrayProperties properties) {
        this.properties = properties;
    }

    /**
     * Check boolean.
     *
     * @param request the request
     * @return the boolean
     */
    public Boolean check(ServletRequest request) {
        return true;
    }
}
