package cn.gmlee.tools.im.core;

import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 主题路由服务.
 *
 * @param <ID>  the type parameter
 * @param <MSG> the type parameter
 */
@RequiredArgsConstructor
public class TopicRouter<ID, MSG> implements Router {

    private final List<Publisher<ID, MSG>> publishers;
    private final List<Subscriber<Flux<MSG>>> subscribers;

    /**
     * Push serializable.
     *
     * @param topic     the topic
     * @param urlParams the url params
     * @param msg       the msg
     * @return the serializable
     */
    public ID push(String topic, MultiValueMap<String, String> urlParams, MSG msg) {
        return route(topic, publishers).push(urlParams, msg);
    }

    /**
     * Pull flux.
     *
     * @param topic     the topic
     * @param urlParams the url params
     * @return the flux
     */
    public Flux<MSG> pull(String topic, MultiValueMap<String, String> urlParams) {
        return route(topic, subscribers).pull(urlParams);
    }
}
