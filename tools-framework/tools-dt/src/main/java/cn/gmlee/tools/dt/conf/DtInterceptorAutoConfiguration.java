package cn.gmlee.tools.dt.conf;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.HttpUtil;
import cn.gmlee.tools.dt.core.TxSupport;
import cn.gmlee.tools.dt.enums.DtHead;
import cn.gmlee.tools.dt.server.TxServer;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 事务管理配置类.
 */
public class DtInterceptorAutoConfiguration {

    /**
     * 通用拦截器.
     *
     * @return the filter registration bean
     */
    @Bean("registration-DtFilter")
    @ConditionalOnClass(Filter.class)
    public FilterRegistrationBean<CorsFilter> filterRegistrationBean() {
        FilterRegistrationBean register = new FilterRegistrationBean();
        register.setFilter((ServletRequest request, ServletResponse response, FilterChain chain) -> {
            if (request instanceof HttpServletRequest) {
                String globalCode = ((HttpServletRequest) request).getHeader(DtHead.GLOBAL_CODE);
                String superiorCode = ((HttpServletRequest) request).getHeader(DtHead.SUPERIOR_CODE);
                TxSupport.saveGlobalCode(globalCode);
                TxSupport.saveSuperiorCode(superiorCode);
            }
            try {
                chain.doFilter(request, response);
            } finally {
                TxSupport.clear();
            }
        });
        register.addUrlPatterns(new String[]{"/*"});
        return register;
    }

    /**
     * HttpClient支持.
     */
    @Configuration
    @ConditionalOnClass(HttpRequestInterceptor.class)
    public static class HttpClient {
        /**
         * Http request interceptor http request interceptor.
         *
         * @return the http request interceptor
         */
        @Bean
        public HttpRequestInterceptor httpRequestInterceptor(TxServer txServer) {
            // HttpClient请求拦截器
            HttpRequestInterceptor dtHeadInterceptor = (HttpRequest request, HttpContext context) -> {
                String globalCode = TxSupport.getGlobalCode();
                if (BoolUtil.notEmpty(globalCode)) {
                    request.addHeader(DtHead.GLOBAL_CODE, globalCode);
                }
                String superiorCode = TxSupport.getSuperiorCode();
                if (BoolUtil.notEmpty(superiorCode)) {
                    txServer.autoIncrementCount(superiorCode);
                    request.addHeader(DtHead.SUPERIOR_CODE, superiorCode);
                }
            };
            HttpUtil.reqInterceptors.add(dtHeadInterceptor);
            return dtHeadInterceptor;
        }
    }

    /**
     * OpenFeign支持.
     */
    @Configuration
    @ConditionalOnClass(RequestInterceptor.class)
    public static class OpenFeign {
        /**
         * Request interceptor request interceptor.
         *
         * @return the request interceptor
         */
        @Bean
        public RequestInterceptor requestInterceptor(TxServer txServer) {
            // OpenFeign请求拦截器
            return (RequestTemplate template) -> {
                String globalCode = TxSupport.getGlobalCode();
                if (BoolUtil.notEmpty(globalCode)) {
                    template.header(DtHead.GLOBAL_CODE, globalCode);
                }
                String superiorCode = TxSupport.getSuperiorCode();
                if (BoolUtil.notEmpty(superiorCode)) {
                    txServer.autoIncrementCount(superiorCode);
                    template.header(DtHead.SUPERIOR_CODE, superiorCode);
                }
            };
        }
    }
}
