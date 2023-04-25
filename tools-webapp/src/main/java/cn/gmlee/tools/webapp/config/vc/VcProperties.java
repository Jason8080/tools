package cn.gmlee.tools.webapp.config.vc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 注册中心的配置类
 *
 * @author Jas °
 * @date 2020 /11/30 (周一)
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tools.webapp.vc")
public class VcProperties {
    /**
     * 请求头中的验证码编号
     */
    private String idHeader = "vc-id";
    /**
     * 请求头中的验证码(用户输入的内容)
     */
    private String vcHeader = "vc";
    /**
     * 用于存储发放的验证码
     */
    private String redisKey = "REDIS:VC:ID_";

    /**
     * 验证码有效期: 1分钟
     */
    private Long vcExpire = 1000L * 60 * 1;


    /**
     * 验证码长度: 默认4位.
     * <p>
     * 数字+大小写字母
     * </p>
     */
    private Integer length = 4;
    /**
     * 图片宽度
     */
    private Integer width = 100;
    /**
     * 图片高度
     */
    private Integer height = 50;



    // -------------------------------------------------



    /**
     * 观察业务数据的有效期: 60分钟
     */
    private Long observationExpire = 1000L * 60 * 60;
    /**
     * 观察业务数据的次数: 3次.
     * <p>
     * 大于3次则必须输入验证码.
     * </p>
     */
    private Integer observationCount = 3;
    /**
     * 用于观察业务数据是否需要输入验证码
     */
    private String observationRedisKey = "REDIS:VC:OBSERVATION:KEY_";
}
