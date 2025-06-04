package cn.gmlee.tools.ai;

import com.alibaba.dashscope.audio.asr.translation.TranslationRecognizerParam;
import com.alibaba.dashscope.audio.asr.translation.TranslationRecognizerRealtime;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args) throws NoApiKeyException {
        // 创建一个Flowable<ByteBuffer>
        String targetLanguage = "en";
        Flowable<ByteBuffer> audioSource =
                Flowable.create(
                        emitter -> {
                            new Thread(
                                    () -> {
                                        try {
                                            // 创建音频格式
                                            AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
                                            // 根据格式匹配默认录音设备
                                            TargetDataLine targetDataLine =
                                                    AudioSystem.getTargetDataLine(audioFormat);
                                            targetDataLine.open(audioFormat);
                                            // 开始录音
                                            targetDataLine.start();
                                            System.out.println("请您通过麦克风讲话体验实时语音识别和翻译功能");
                                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                                            long start = System.currentTimeMillis();
                                            // 录音50s并进行实时识别
                                            while (System.currentTimeMillis() - start < 50000) {
                                                int read = targetDataLine.read(buffer.array(), 0, buffer.capacity());
                                                if (read > 0) {
                                                    buffer.limit(read);
                                                    // 将录音音频数据发送给流式识别服务
                                                    emitter.onNext(buffer);
                                                    buffer = ByteBuffer.allocate(1024);
                                                    // 录音速率有限，防止cpu占用过高，休眠一小会儿
                                                    Thread.sleep(20);
                                                }
                                            }
                                            // 通知结束
                                            emitter.onComplete();
                                        } catch (Exception e) {
                                            emitter.onError(e);
                                        }
                                    })
                                    .start();
                        },
                        BackpressureStrategy.BUFFER);

        // 创建Recognizer
        TranslationRecognizerRealtime translator = new TranslationRecognizerRealtime();
        // 创建RecognitionParam，audioFrames参数中传入上面创建的Flowable<ByteBuffer>
        TranslationRecognizerParam param =
                TranslationRecognizerParam.builder()
                        // 若没有将API Key配置到环境变量中，需将your-api-key替换为自己的API Key
                         .apiKey("sk-04e3b0b9d222438b9be1422be980bbfe")
                        .model("gummy-realtime-v1")
                        .format("pcm") // 'pcm'、'wav'、'mp3'、'opus'、'speex'、'aac'、'amr', you
                        // can check the supported formats in the document
                        .sampleRate(16000)
                        .transcriptionEnabled(true)
                        .sourceLanguage("auto")
                        .translationEnabled(true)
                        .translationLanguages(new String[] {targetLanguage})
                        .build();

        // 流式调用接口
        translator
                .streamCall(param, audioSource)
                // 调用Flowable的blockingForEachc处理结果
                .blockingForEach(
                        result -> {
                            if (result.getTranscriptionResult() == null) {
                                return;
                            }
                            try {
                                System.out.println("RequestId: " + result.getRequestId());
                                // 打印最终结果
                                if (result.getTranscriptionResult() != null) {
                                    System.out.println("Transcription Result:");
                                    if (result.isSentenceEnd()) {
                                        System.out.println("\tFix:" + result.getTranscriptionResult().getText());
                                        System.out.println("\tStash:" + result.getTranscriptionResult().getStash());
                                    } else {
                                        System.out.println("\tTemp Result:" + result.getTranscriptionResult().getText());
                                    }
                                }
                                if (result.getTranslationResult() != null) {
                                    System.out.println("English Translation Result:");
                                    if (result.isSentenceEnd()) {
                                        System.out.println("\tFix:" + result.getTranslationResult().getTranslation(targetLanguage).getText());
                                        System.out.println("\tStash:" + result.getTranslationResult().getTranslation(targetLanguage).getStash());
                                    } else {
                                        System.out.println("\tTemp Result:" + result.getTranslationResult().getTranslation(targetLanguage).getText());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
        System.exit(0);
    }
}