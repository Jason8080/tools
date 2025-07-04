package cn.gmlee.tools.microphone.controller;

import org.reactivestreams.Publisher;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 麦克风接口.
 *
 * @param <B> 请求体
 * @param <D> 响应体
 */
@Controller
public interface MicrophoneController<B, D> {
    /**
     * 连接处理器.
     *
     * @param headers the headers
     * @return the mono
     */
    @ConnectMapping("/microphone/connection")
    default Mono<Void> connection(@Header Map<String, Object> headers) {
        return Mono.empty();
    }


    /**
     * 心跳处理器.
     *
     * @param headers the headers
     * @return the mono
     */
    @MessageMapping("/microphone/heartbeat")
    default Mono<Void> heartbeat(@Header Map<String, Object> headers) {
        return Mono.empty();
    }
    /**
     * 双工处理器.
     *
     * <p>输出JSON的dataMimeType: application/stream+json</p>
     *
     * @param headers   请求头
     * @param publisher 输入流
     * @return publisher 输出流
     */
    @MessageMapping("/microphone")
    Publisher<D> channel(@Header Map<String, Object> headers, @Validated @Payload Publisher<B> publisher);
}