package cn.gmlee.tools.request.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 加解密配置
 *
 * @author Jas °
 * @date 2020 /11/25 (周三)
 */
@Data
@ConfigurationProperties(prefix = "tools.request.ed")
public class EdProperties {
    /**
     * 是否必须加密/解密成功.
     */
    private Boolean must = false;
    /**
     * 公钥.
     */
    private String publicKey;
    /**
     * 私钥.
     */
    private String privateKey;
    /**
     * 请求头中的安全码属性名.
     * <p>
     * 该属性值由前端生成并采用公钥加密传输.
     * </p>
     */
    private String ciphertext = "edc";
}
