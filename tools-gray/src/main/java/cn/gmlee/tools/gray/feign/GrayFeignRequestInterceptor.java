package cn.gmlee.tools.gray.feign;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.gray.conf.GrayProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * The type Gray request interceptor.
 */
public class GrayFeignRequestInterceptor implements RequestInterceptor {

    private final GrayProperties properties;

    /**
     * Instantiates a new Gray request interceptor.
     *
     * @param properties the properties
     */
    public GrayFeignRequestInterceptor(GrayProperties properties) {
        this.properties = properties;
    }

    @Override
    public void apply(RequestTemplate template) {
        // 令牌透传
        String token = WebUtil.getCurrentHeader(properties.getToken());
        if (BoolUtil.isEmpty(token)) {
            return;
        }
        template.header(properties.getToken(), token);
        // 版本号透传
        String version = WebUtil.getCurrentHeader(properties.getHead());
        if (BoolUtil.isEmpty(version)) {
            return;
        }
        template.header(properties.getHead(), version);
        // 客户地址透传
        String host = WebUtil.getCurrentHeader(WebUtil.REMOTE_ADDRESS_HOST);
        if(BoolUtil.isEmpty(version)){
            return;
        }
        template.header(WebUtil.REMOTE_ADDRESS_HOST, host);
    }
}
