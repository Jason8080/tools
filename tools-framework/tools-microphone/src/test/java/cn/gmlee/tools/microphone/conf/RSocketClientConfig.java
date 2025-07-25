package cn.gmlee.tools.microphone.conf;

import cn.gmlee.tools.base.builder.MapBuilder;
import cn.gmlee.tools.base.kit.sound.Microphone;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.ThreadUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.ByteArrayDecoder;
import org.springframework.core.codec.ByteArrayEncoder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

@Configuration
public class RSocketClientConfig {

    @Bean
    public Mono<RSocketRequester> rSocketRequester(RSocketRequester.Builder builder) {
        return builder
            .rsocketStrategies(strategies -> strategies
                .encoder(new Jackson2JsonEncoder(), new ByteArrayEncoder())
                .decoder(new Jackson2JsonDecoder(), new ByteArrayDecoder())
            )
            .setupRoute("/microphone/ai_translator/connection")
            .setupMetadata(MapBuilder.of("token","4BC6EACA0429421CBBFAE48FA4E506A3", "language", "en"), MediaType.APPLICATION_JSON)
            .dataMimeType(MediaType.APPLICATION_STREAM_JSON)
            .connectWebSocket(URI.create("wss://ai.gmlee.cn/rsocket")); // WebSocket端点
    }

    @Component
    public static class SpeechClient {
        
        private final Mono<RSocketRequester> requesterMono;

        public SpeechClient(Mono<RSocketRequester> requesterMono) {
            this.requesterMono = requesterMono;
        }

        public Flux<Map> streamSpeech(String token, String language, Flux<byte[]> audioStream) {
            // 创建元数据
            Map<String, String> metadata = MapBuilder.of("token",token, "language", language);
            
            // 创建元数据MimeType
            MimeType metadataMimeType = MimeTypeUtils.APPLICATION_JSON;
            
            return requesterMono.flatMapMany(requester ->
                requester
                    .route("/microphone/ai_translator")
                    .metadata(metadata, metadataMimeType) // JSON元数据
                    .data(audioStream) // 直接发送字节数组流
                    .retrieveFlux(Map.class)
            );
        }

        // 测试方法
        public void testSpeechStream() throws Exception {
            // 测试元数据
            String authToken = "4BC6EACA0429421CBBFAE48FA4E506A3";
            String language = "en";
            Microphone microphone = new Microphone();
            // 模拟音频流 (每20ms发送一个区块)
            ThreadUtil.execute(() -> ExceptionUtil.suppress(() -> recoding(microphone)));
            Flowable<ByteBuffer> audioSource = Flowable.create(emitter -> microphone.start(emitter), BackpressureStrategy.BUFFER);
            // 发送请求并处理响应
            streamSpeech(authToken, language, Flux.from(audioSource).map(byteBuffer -> byteBuffer.array()))
                .doOnSubscribe(sub -> System.out.println("开始语音识别..."))
                .doOnNext(json -> System.out.println("实时结果: " + json))
                .doOnError(e -> System.err.println("识别错误: " + e.getMessage()))
                .doOnComplete(() -> System.out.println("识别完成"))
                .blockLast() // 阻塞等待完成 (测试用)
            ;
        }

        private void recoding(Microphone microphone) throws Exception {
            long millis = System.currentTimeMillis() + 5000;
            // 创建音频
            AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
            // 根据格式匹配默认录音设备
            TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
            targetDataLine.open(audioFormat);
            // 开始录音
            targetDataLine.start();
            while (millis > System.currentTimeMillis()) {
                byte[] bytes = new byte[1024];
                int read = targetDataLine.read(bytes, 0, bytes.length);
                if(read > 0){
                    microphone.write(Arrays.copyOfRange(bytes, 0, read));
                }
            }
            microphone.exit();
            targetDataLine.close();
        }
    }

    // 主应用入口
    @SpringBootApplication
    public static class RSocketClientApp implements CommandLineRunner {
        
        private final SpeechClient speechClient;
        
        public RSocketClientApp(SpeechClient speechClient) {
            this.speechClient = speechClient;
        }
        
        public static void main(String[] args) {
            SpringApplication.run(RSocketClientApp.class, args);
        }
        
        @Override
        public void run(String... args) throws Exception {
            speechClient.testSpeechStream();
        }
    }
}