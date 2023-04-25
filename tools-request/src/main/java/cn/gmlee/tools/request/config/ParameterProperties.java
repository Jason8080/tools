package cn.gmlee.tools.request.config;

import cn.gmlee.tools.request.mod.Desensitization;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

/**
 * 通用数据源属性配置
 *
 * @author Jas°
 * @date 2020/11/25 (周三)
 */
@Data
@ConfigurationProperties(prefix = "tools.request")
public class ParameterProperties {
    /**
     * 脱敏规则
     */
    private List<Desensitization> desensitization = new ArrayList(0);
    /**
     * 不脱敏的地址
     */
    private List<String> desensitizationUrlExcludes = new ArrayList(0);
    /**
     * 不处理的类
     */
    private List<Class<?>> classExcludes = new ArrayList(0);
}
