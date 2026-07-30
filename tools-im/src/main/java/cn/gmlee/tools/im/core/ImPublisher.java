package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

/**
 * Publisher 框架骨架.
 * <p>
 * 继承 {@link AbstractTopic}，提供 TopicMessage 构建、Repeater 委托和日志记录。
 * 子类只需提供构造器即可组成可用的默认发布器。
 * </p>
 *
 * <h3>扩展方式</h3>
 * <p>
 * 继承此类即可获得 Repeater 解析和日志能力。
 * 可重写 {@link #push(MultiValueMap, Msg)} 自定义发布逻辑。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
public abstract class ImPublisher extends AbstractTopic implements Publisher {

    protected ImPublisher(String topic, Repeater repeater) {
        super(topic, repeater);
    }

    protected ImPublisher(String topic, Supplier<Repeater> repeaterSupplier) {
        super(topic, repeaterSupplier);
    }

    @Override
    public Serializable push(MultiValueMap<String, String> urlParams, Msg msg) {
        TopicMessage<Msg> event = msg.build(urlParams);
        event.setTopic(topic);
        // 从 URL 参数提取定向投递目标（?to=alice&to=bob）
        List<String> toList = urlParams.get("to");
        if (toList != null && !toList.isEmpty()) {
            event.setTo(new HashSet<>(toList));
        }
        Serializable id = resolveRepeater().send(event);
        log.debug("[ImPublisher] 发布消息: topic={}, id={}", topic, id);
        return id;
    }
}
