package cn.gmlee.tools.im.spi.interceptor;

import cn.gmlee.tools.im.model.ConnectionMetadata;
import lombok.Builder;
import lombok.Value;
import org.springframework.util.MultiValueMap;

/**
 * 订阅上下文（{@code transformSubscribeStream} 入参）.
 * <p>
 * 聚合订阅时刻的全部环境信息，避免拦截器方法签名随框架演进而频繁变化。
 * 不可变，线程安全。
 * </p>
 *
 * @since 5.7.0
 */
@Value
@Builder
public class SubscribeContext {

    /**
     * Topic 名称
     */
    String topic;

    /**
     * 客户端 URL 查询参数（不含 Last-Event-ID，该值仅通过请求头接收）
     */
    MultiValueMap<String, String> urlParams;

    /**
     * 连接元数据（身份、连接 ID、续传位点等）
     */
    ConnectionMetadata metadata;

    /**
     * 客户端断线续传位点（最后收到的消息 ID 的字符串形式）.
     *
     * @return Last-Event-ID；首次订阅或客户端未携带时为 null
     */
    public String lastEventId() {
        return metadata != null ? metadata.getLastEventId() : null;
    }

    /**
     * 便捷构造.
     *
     * @param topic     Topic 名称
     * @param urlParams URL 参数
     * @param metadata  连接元数据
     * @return 订阅上下文
     */
    public static SubscribeContext of(String topic, MultiValueMap<String, String> urlParams,
                                      ConnectionMetadata metadata) {
        return SubscribeContext.builder()
                .topic(topic)
                .urlParams(urlParams)
                .metadata(metadata)
                .build();
    }
}
