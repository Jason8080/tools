package cn.gmlee.tools.api.gray;

import cn.gmlee.tools.api.gray.model.Gray;

import java.util.List;
import java.util.Set;

/**
 * 灰度过滤器接口定义
 *
 * @param <Request>  the type parameter
 * @param <Response> the type parameter
 * @author Jas°
 * @date 2020 /12/3 (周四)
 */
public interface GrayFilter<Request, Response> {

    /**
     * Gets gray.
     *
     * @return the gray
     */
    List<Gray> getGrays();

    /**
     * 过滤所有请求.
     *
     * @param request  the request
     * @param response the response
     */
    void doGray(Request request, Response response);

    /**
     * Gets ip.
     *
     * @param request the request
     * @return the ip
     */
    String getIp(Request request);

    /**
     * Gets version.
     *
     * @param request the request
     * @return the version
     */
    String getVersion(Request request);

    /**
     * Gets url.
     *
     * @param request the request
     * @return the url
     */
    String getUrl(Request request);

    /**
     * Gets token.
     *
     * @param request the request
     * @return the token
     */
    String getToken(Request request);

    /**
     * Gets fixed urls.
     *
     * @param request the request
     * @return the fixed urls
     */
    Set<String> getFixedUrls(Request request);
}
