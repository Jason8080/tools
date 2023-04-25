package cn.gmlee.tools.api.gray.model;

import lombok.Data;

/**
 * 灰度请求实体
 *
 * @author Jas°
 * @date 2020/12/3 (周四)
 */
@Data
public class GrayRequest {
    /**
     * 请求IP
     */
    String ip;
    /**
     * 请求Token
     */
    String token;
    /**
     * 请求资源
     */
    String url;
    /**
     * 请求资源版本号
     */
    String version;
}
