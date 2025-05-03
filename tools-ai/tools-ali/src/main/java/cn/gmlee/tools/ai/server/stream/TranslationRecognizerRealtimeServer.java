package cn.gmlee.tools.ai.server.stream;

import cn.gmlee.tools.ai.conf.AliAiProperties;
import cn.gmlee.tools.base.kit.sound.Microphone;
import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import com.alibaba.dashscope.audio.asr.translation.TranslationRecognizerParam;
import com.alibaba.dashscope.audio.asr.translation.TranslationRecognizerRealtime;
import com.alibaba.dashscope.audio.asr.translation.results.TranscriptionResult;
import com.alibaba.dashscope.audio.asr.translation.results.Translation;
import com.alibaba.dashscope.audio.asr.translation.results.TranslationRecognizerResult;
import com.alibaba.dashscope.audio.asr.translation.results.TranslationResult;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * 百炼平台服务.
 */
@Slf4j
@RequiredArgsConstructor
public class TranslationRecognizerRealtimeServer {

    private final AliAiProperties aliAiProperties;

    private final TranslationRecognizerRealtime ali = new TranslationRecognizerRealtime();

    /**
     * 询问.
     *
     * @param microphone 麦克风
     * @param languages  目标语种
     * @return flowable 输出内容 key原文、val<语种,译文>
     */
    public Flowable<Kv<String, Map<String, String>>> ask(Microphone microphone, String... languages) {
        TranslationRecognizerParam param = getTranslationRecognizerParam(aliAiProperties.getDefaultModel(), languages);
        Flowable<ByteBuffer> audioSource = Flowable.create(emitter -> microphone.start(emitter), BackpressureStrategy.BUFFER);
        Flowable<TranslationRecognizerResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param, audioSource));
        return flowable.map(this::convertText);
    }

    /**
     * 询问.
     *
     * @param model      模型
     * @param microphone 麦克风
     * @param languages  目标语种
     * @return flowable 输出内容 key原文、val<语种,译文>
     */
    public Flowable<Kv<String, Map<String, String>>> ask(String model, Microphone microphone, String... languages) {
        TranslationRecognizerParam param = getTranslationRecognizerParam(model, languages);
        Flowable<ByteBuffer> audioSource = Flowable.create(emitter -> microphone.start(emitter), BackpressureStrategy.BUFFER);
        Flowable<TranslationRecognizerResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param, audioSource));
        return flowable.map(this::convertText);
    }

    private TranslationRecognizerParam getTranslationRecognizerParam(String model, String... languages) {
        return TranslationRecognizerParam.builder()
                .apiKey(aliAiProperties.getApiKey())
                .format(aliAiProperties.getAudioFormat())
                .model(model)
                .sampleRate(16000) // 设置待识别音频采样率（单位Hz）。支持16000Hz及以上采样率。
                .transcriptionEnabled(true) // 设置是否开启实时识别
                .sourceLanguage("auto") // 设置源语言（待识别/翻译语言）代码
                .translationEnabled(true) // 设置是否开启实时翻译
                .translationLanguages(languages) // 输出语言
                .build();
    }

    private Kv<String, Map<String, String>> convertText(TranslationRecognizerResult result) {
        TranscriptionResult input = result.getTranscriptionResult(); // 获取输入内容
        TranslationResult output = result.getTranslationResult(); // 获取输出内容
        Kv<String, Map<String, String>> kv = new Kv();
        kv.setKey(input.isSentenceEnd() ? NullUtil.get(input.getText()) : ""); // 收集输入内容
        Map<String, Translation> translationMap = output.getTranslations();
        Map<String, String> outputMap = new HashMap<>(translationMap.size()); // 收集输出内容
        for (Map.Entry<String, Translation> entry : translationMap.entrySet()) {
            String key = entry.getKey(); // 输出语言
            Translation value = entry.getValue();
            String val = value.isSentenceEnd() ? NullUtil.get(value.getText()) : ""; // 收集输出内容
            outputMap.put(key, val);
        }
        kv.setVal(outputMap);
        return kv;
    }
}
