package cn.gmlee.tools.ds.config.druid;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * Druid配置类
 *
 * @author Jas°
 * @date 2020/8/31 (周一)
 */
@PropertySource(value = {"classpath:mysql.properties","classpath:application.properties"}, ignoreResourceNotFound = true)
public class DruidMonitorAutoConfiguration {

    @Value("${login-username:druid}")
    private String username;
    @Value("${login-password:druid}")
    private String password;

    @Bean
    public ServletRegistrationBean druidStatViewServlet() {
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(new StatViewServlet(), new String[]{"/druid/*"});
        registrationBean.addInitParameter("allow", "");
        registrationBean.addInitParameter("deny", "");
        registrationBean.addInitParameter("loginUsername", username);
        registrationBean.addInitParameter("loginPassword", password);
        registrationBean.addInitParameter("resetEnable", "false");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean druidWebStatViewFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean(new WebStatFilter(), new ServletRegistrationBean[0]);
        registrationBean.addInitParameter("urlPatterns", "/*");
        registrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*");
        return registrationBean;
    }
}
