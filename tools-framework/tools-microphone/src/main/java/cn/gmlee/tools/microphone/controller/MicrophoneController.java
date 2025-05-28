package cn.gmlee.tools.microphone.controller;

import org.reactivestreams.Publisher;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * 麦克风接口.
 *
 * @param <B>     请求体
 * @param <D>     响应体
 */
@Controller
public interface MicrophoneController<B, D> {
    /**
     * 双工处理器.
     *
     * @param headers 请求头
     * @param flux    输入流
     * @return flux 输出流
     */
    @MessageMapping("/microphone")
    Publisher<D> channel(@Headers Map<String, Object> headers, @Validated @Payload Publisher<B> flux);
}