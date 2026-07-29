package cn.gmlee.tools.im.core;

import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 主题路由服务.
 *
 * @param <ID>  消息 ID 类型
 * @param <MSG> 消息类型
 */
@RequiredArgsConstructor
public class TopicRouter<ID, MSG> implements Router {

    private final List<Publisher<ID, MSG>> publishers;
    private final List<Subscriber<MSG>> subscribers;

    /**
     * 推送消息.
     *
     * @param topic     主题名称
     * @param urlParams URL 参数
     * @param msg       消息内容
     * @return 消息 ID
     */
    public ID push(String topic, MultiValueMap<String, String> urlParams, MSG msg) {
        return route(topic, publishers).push(urlParams, msg);
    }

    /**
     * 拉取消息流.
     *
     * @param topic     主题名称
     * @param urlParams URL 参数
     * @return 消息响应式流
     */
    public Flux<MSG> pull(String topic, MultiValueMap<String, String> urlParams) {
        return route(topic, subscribers).pull(urlParams);
    }
}
