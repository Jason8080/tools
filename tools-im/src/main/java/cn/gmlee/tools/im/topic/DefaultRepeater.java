package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.ImRepeater;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.spi.interceptor.RepeaterInterceptor;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;

import java.io.Serializable;
import java.util.List;

/**
 * 默认消息转发器（包级私有）.
 * <p>
 * 继承 {@link ImRepeater} 框架骨架，拦截器自动织入。
 * 直接使用父类提供的 IM 标准实现，无需重写任何方法。
 * 仅供 {@link TopicRegistry} 内部使用。
 * </p>
 * <p>
 * 使用具体类型 {@code ImRepeater<Serializable, Msg>}，使公共 façade 的
 * {@code send()} 返回 {@code Mono<Serializable>}，所有 unchecked cast 退化为 no-op。
 * </p>
 * <p>
 * 自定义转发器应继承 {@link ImRepeater}，并通过 {@link cn.gmlee.tools.im.spi.factory.RepeaterFactory} 创建。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
class DefaultRepeater extends ImRepeater<Serializable, Msg> {

    public DefaultRepeater(String topic,
                           StreamBridge streamBridge,
                           SseConnectionManager sseConnectionManager,
                           List<RepeaterInterceptor> interceptors) {
        super(topic, streamBridge,
              sseConnectionManager::publish,
              sseConnectionManager::subscribe,
              interceptors);
    }
}
