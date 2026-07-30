package cn.gmlee.tools.im.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * IM 框架配置属性.
 * <p>
 * 配置前缀：{@code im}
 * </p>
 *
 * <h3>典型配置</h3>
 * <pre>{@code
 * im:
 *   endpoints:
 *     - path: /api/chat/pull
 *       topic: im.chat
 *       mode: pull
 *     - path: /api/chat/push
 *       topic: im.chat
 *       mode: push
 * }</pre>
 *
 * @since 5.6.0
 */
@Data
@ConfigurationProperties(prefix = "im")
public class ImProperties {

    /**
     * 端点配置列表.
     * <p>
     * 每个端点定义一个 URL 路径到内部 Topic 的映射。
     * 框架在启动时加载此列表，自动注册端点和创建 Stream 资源。
     * </p>
     */
    private List<EndpointProperties> endpoints = new ArrayList<>();
}
