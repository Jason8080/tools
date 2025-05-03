package cn.gmlee.tools.ai.conf;

import cn.gmlee.tools.ai.server.async.ImageSynthesisServer;
import cn.gmlee.tools.ai.server.stream.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AliAiProperties.class)
public class AliAiAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ImageSynthesisServer.class)
    public ImageSynthesisServer imageSynthesisServer(AliAiProperties aliAiProperties) {
        return new ImageSynthesisServer(aliAiProperties);
    }

    @Bean
    @ConditionalOnMissingBean(TranslationRecognizerRealtimeServer.class)
    public TranslationRecognizerRealtimeServer translationRecognizerRealtimeServer(AliAiProperties aliAiProperties) {
        return new TranslationRecognizerRealtimeServer(aliAiProperties);
    }

    @Bean
    @ConditionalOnMissingBean(RecognitionServer.class)
    public RecognitionServer recognitionServer(AliAiProperties aliAiProperties) {
        return new RecognitionServer(aliAiProperties);
    }

    @Bean
    @ConditionalOnMissingBean(ApplicationServer.class)
    public ApplicationServer applicationServer(AliAiProperties aliAiProperties) {
        return new ApplicationServer(aliAiProperties);
    }

    @Bean
    @ConditionalOnMissingBean(GenerateServer.class)
    public GenerateServer generateServer(AliAiProperties aliAiProperties) {
        return new GenerateServer(aliAiProperties);
    }

    @Bean
    @ConditionalOnMissingBean(DashScopeServer.class)
    public DashScopeServer dashScopeServer(AliAiProperties aliAiProperties) {
        return new DashScopeServer(aliAiProperties);
    }

    @Bean
    @ConditionalOnMissingBean(MultiModalConversationServer.class)
    public MultiModalConversationServer multiModalConversationServer(AliAiProperties aliAiProperties) {
        return new MultiModalConversationServer(aliAiProperties);
    }
}
