package cn.gmlee.tools.bit.conf;

import cn.gmlee.tools.bit.server.stream.MultiModalConversationServer;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * The type Bit ai auto configuration.
 */
@AutoConfigureAfter(BitAutoConfiguration.class)
@EnableConfigurationProperties(BitAiProperties.class)
public class BitAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MultiModalConversationServer.class)
    public MultiModalConversationServer multiModalConversationServer(ArkService arkService, BitAiProperties bitAiProperties) {
        return new MultiModalConversationServer(arkService, bitAiProperties);
    }
}
