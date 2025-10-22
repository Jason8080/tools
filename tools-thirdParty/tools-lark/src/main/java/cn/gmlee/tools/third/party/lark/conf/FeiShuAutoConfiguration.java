package cn.gmlee.tools.third.party.lark.conf;

import com.lark.oapi.Client;
import com.lark.oapi.core.enums.BaseUrlEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * The type Fei shu auto configuration.
 */
@RequiredArgsConstructor
@EnableConfigurationProperties(FeiShuProperties.class)
public class FeiShuAutoConfiguration {

    private final FeiShuProperties feiShuProperties;

    /**
     * Fei shu client client.
     *
     * @return the client
     */
    @Bean
    public Client FeiShuClient() {
        return Client.newBuilder(feiShuProperties.getAppId(), feiShuProperties.getAppSecret())
                .openBaseUrl(BaseUrlEnum.FeiShu) // 设置域名，默认为飞书
                .requestTimeout(3, TimeUnit.SECONDS) // 设置httpclient 超时时间，默认永不超时
                .logReqAtDebug(true) // 在 debug 模式下会打印 http 请求和响应的 headers、body 等信息。
                .build();
    }

}
