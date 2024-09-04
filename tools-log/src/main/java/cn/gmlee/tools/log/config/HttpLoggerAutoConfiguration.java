package cn.gmlee.tools.log.config;

import cn.gmlee.tools.log.http.HttpClientInterceptor;
import cn.gmlee.tools.log.http.OkHttpInterceptor;
import okhttp3.Interceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;

public class HttpLoggerAutoConfiguration {

    @Bean
    @ConditionalOnClass(Interceptor.class)
    public OkHttpInterceptor okHttpInterceptor(){
        return new OkHttpInterceptor();
    }

    @Bean
    @ConditionalOnClass(ClientHttpRequestInterceptor.class)
    public HttpClientInterceptor clientHttpRequestInterceptor(){
        return new HttpClientInterceptor();
    }
}
