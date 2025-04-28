package cn.gmlee.tools.ai.server.impl;

import cn.gmlee.tools.ai.conf.AliAiProperties;
import cn.gmlee.tools.base.kit.sound.Microphone;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionResult;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

/**
 * 实时语音转文字.
 *
 * <p>paraformer-realtime-v2</p>
 */
@Slf4j
@RequiredArgsConstructor
public class RecognitionServer {

    private final AliAiProperties aliAiProperties;

    private final Recognition ali = new Recognition();

    public Flowable<String> ask(Microphone microphone) {
        RecognitionParam param = getRecognitionParam(aliAiProperties.getDefaultModel());
        Flowable<ByteBuffer> audioSource = Flowable.create(emitter -> microphone.start(emitter), BackpressureStrategy.BUFFER);
        Flowable<RecognitionResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param, audioSource));
        return flowable.map(this::convertText);
    }


    public Flowable<String> ask(String model, Microphone microphone) {
        RecognitionParam param = getRecognitionParam(model);
        Flowable<ByteBuffer> audioSource = Flowable.create(emitter -> microphone.start(emitter), BackpressureStrategy.BUFFER);
        Flowable<RecognitionResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param, audioSource));
        return flowable.map(this::convertText);
    }

    private RecognitionParam getRecognitionParam(String model) {
        return RecognitionParam.builder()
                .apiKey(aliAiProperties.getApiKey())
                .format(aliAiProperties.getAudioFormat())
                .sampleRate(16000)
                .model(model)
                .build();
    }

    private String convertText(RecognitionResult result) {
        if (result.isSentenceEnd()) {
            return NullUtil.get(result.getSentence().getText());
        }
        return "";
    }
}
