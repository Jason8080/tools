package cn.gmlee.tools.im.serve;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.MsgEvent;
import cn.gmlee.tools.im.core.Subscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * 订阅服务
 */
@RequiredArgsConstructor
public class SubscriberServe implements Subscriber<Flux<R<Msg>>> {

    private final Sinks.Many<MsgEvent<Msg>> sinksMany;

    @Override
    public String topic() {
        return "";
    }

    @Override
    public Flux<R<Msg>> pull(MultiValueMap<String, String> urlParams) {
        return Flux.empty();
    }

}
