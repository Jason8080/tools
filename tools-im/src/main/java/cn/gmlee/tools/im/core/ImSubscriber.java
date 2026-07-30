package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.Msg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

/**
 * Subscriber 框架骨架.
 * <p>
 * 继承 {@link AbstractTopic}，提供 Repeater 委托和日志记录。
 * 子类只需提供构造器即可组成可用的默认订阅器。
 * </p>
 *
 * <h3>扩展方式</h3>
 * <p>
 * 继承此类即可获得 Repeater 解析和日志能力。
 * 可重写 {@link #pull(MultiValueMap)} 自定义订阅逻辑。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
public abstract class ImSubscriber extends AbstractTopic implements Subscriber {

    protected ImSubscriber(String topic, Repeater repeater) {
        super(topic, repeater);
    }

    protected ImSubscriber(String topic, Supplier<Repeater> repeaterSupplier) {
        super(topic, repeaterSupplier);
    }

    @Override
    public Flux<Msg> pull(MultiValueMap<String, String> urlParams) {
        log.debug("[ImSubscriber] 订阅消息流: topic={}", topic);
        return resolveRepeater().subscribe(urlParams);
    }
}
