package cn.gmlee.tools.log.config;

import cn.gmlee.tools.log.http.HttpClientInterceptor;
import cn.gmlee.tools.log.http.OkHttpInterceptor;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@ImportAutoConfiguration({
        HttpLoggerAutoConfiguration.OkHttpAutoConfiguration.class,
        HttpLoggerAutoConfiguration.HttpClientAutoConfiguration.class,
})
public class HttpLoggerAutoConfiguration {

    @ConditionalOnClass(name = "okhttp3.Interceptor")
    public static class OkHttpAutoConfiguration {
        @Bean
        public OkHttpInterceptor okHttpInterceptor(){
            return new OkHttpInterceptor();
        }
    }

    @ConditionalOnClass(name = "org.springframework.http.client.ClientHttpRequestInterceptor")
    public static class HttpClientAutoConfiguration {
        @Bean
        public HttpClientInterceptor clientHttpRequestInterceptor(){
            return new HttpClientInterceptor();
        }
    }
}
