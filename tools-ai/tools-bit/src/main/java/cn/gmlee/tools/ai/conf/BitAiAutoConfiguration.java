package cn.gmlee.tools.ai.conf;

import com.volcengine.ark.runtime.service.ArkService;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * The type Bit ai auto configuration.
 */
@EnableConfigurationProperties(BitAiProperties.class)
public class BitAiAutoConfiguration {

    /**
     * Dispatcher dispatcher.
     *
     * @return the dispatcher
     */
    @Bean
    public Dispatcher dispatcher(){
        return new Dispatcher();
    }

    /**
     * Connection pool connection pool.
     *
     * @param bitAiProperties the bit ai properties
     * @return the connection pool
     */
    @Bean
    public ConnectionPool connectionPool(BitAiProperties bitAiProperties){
        return new ConnectionPool(5, 1, TimeUnit.SECONDS);
    }

    /**
     * Ark service ark service.
     *
     * @param bitAiProperties the bit ai properties
     * @return the ark service
     */
    @Bean
    public ArkService arkService(BitAiProperties bitAiProperties) {
        return ArkService.builder()
                .dispatcher(dispatcher())
                .connectionPool(connectionPool(bitAiProperties))
                .apiKey(bitAiProperties.getApiKey())
                .build();
    }
}
