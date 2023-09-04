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
        String head = WebUtil.getCurrentHeader(properties.getHead());
        if (BoolUtil.isEmpty(head)) {
            return;
        }
        template.header(properties.getHead(), head);
    }
}
