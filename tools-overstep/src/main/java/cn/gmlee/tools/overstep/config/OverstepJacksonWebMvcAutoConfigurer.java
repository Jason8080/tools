package cn.gmlee.tools.overstep.config;

import cn.gmlee.tools.overstep.converter.SnMappingJackson2HttpMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

/**
 * .
 *
 * @author Jas°
 * @date 2021/7/28 (周三)
 */
@Import(OverstepWebMvcAutoConfigurer.class)
public class OverstepJacksonWebMvcAutoConfigurer {

    @Resource
    private SnProperties snProperties;

    @Bean
    public SnMappingJackson2HttpMessageConverter snMappingJackson2HttpMessageConverter(ObjectMapper objectMapper){
        return new SnMappingJackson2HttpMessageConverter(objectMapper, snProperties);
    }
}
