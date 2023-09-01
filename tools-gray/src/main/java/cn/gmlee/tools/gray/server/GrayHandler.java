package cn.gmlee.tools.gray.server;

/**
 * 网关灰度请求处理器.
 */
public interface GrayHandler {
    String ip = "ip";
    String user = "user";
    String weight = "weight";
    String custom = "custom";
    /**
     * 处理器名称.
     *
     * @return the string
     */
    String name();

    /**
     * 是否支持并处理.
     *
     * @param app
     * @param token the token
     * @return the boolean
     */
    boolean support(String app, String token);


    /**
     * 是否允许并进入灰度节点.
     *
     * @param app
     * @param token the token
     * @return the boolean
     */
    boolean allow(String app, String token);
}
