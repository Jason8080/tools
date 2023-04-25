package cn.gmlee.tools.es.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;

/**
 * @author Jas°
 * @date 2021/5/6 (周四)
 */
@AutoConfigureAfter(ElasticsearchDataAutoConfiguration.class)
public class ElasticsearchAutoConfiguration {

    @Value("${tools.es.maxIdleTime:1}")
    private Long maxIdleTime;

}
