package cn.gmlee.tools.overstep.config;

import cn.gmlee.tools.overstep.converter.SnLongSnConverter;
import cn.gmlee.tools.overstep.converter.SnIntegerConverter;
import cn.gmlee.tools.overstep.converter.StringSnConverter;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

/**
 * .
 *
 * @author Jas°
 * @date 2021/7/28 (周三)
 */
public class OverstepWebMvcAutoConfigurer {

    @Resource
    private SnProperties snProperties;

    @Bean
    public SnLongSnConverter longSnConverter(){
        return new SnLongSnConverter(snProperties);
    }

    @Bean
    public SnIntegerConverter snIntegerConverter(){
        return new SnIntegerConverter(snProperties);
    }

    @Bean
    public StringSnConverter stringSnConverter(){
        return new StringSnConverter(snProperties);
    }
}
