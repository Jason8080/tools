package cn.gmlee.tools.im.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@EnableConfigurationProperties({
        ImProperties.class, StreamProperties.class, SseProperties.class,
})
public class ImAutoConfiguration {

}
