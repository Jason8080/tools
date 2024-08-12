package cn.gmlee.tools.mybatis.config;

import cn.gmlee.tools.mybatis.interceptor.SqlLoggerInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * IBatis通用装配.
 */
public class IBatisAutoConfiguration {
    /**
     * 真实日志打印.
     *
     * @return the sql logger interceptor
     */
    @Bean
    public SqlLoggerInterceptor sqlLoggerInterceptor() {
        return new SqlLoggerInterceptor();
    }
}
