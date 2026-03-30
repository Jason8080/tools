package cn.gmlee.tools.es.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchAutoConfiguration;

/**
 * @author Jas°
 * @date 2021/5/6 (周四)
 */
@AutoConfigureAfter(DataElasticsearchAutoConfiguration.class)
public class ElasticsearchAutoConfiguration {

    @Value("${tools.es.maxIdleTime:1}")
    private Long maxIdleTime;

}
