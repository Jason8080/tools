package cn.gmlee.tools.overstep.converter;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.SnUtil;
import cn.gmlee.tools.overstep.config.SnProperties;
import org.springframework.core.convert.converter.Converter;

/**
 * .
 *
 * @author Jas°
 * @date 2021/7/28 (周三)
 */
public class SnLongSnConverter implements Converter<String, Long> {
    private final SnProperties snProperties;

    public SnLongSnConverter(SnProperties snProperties) {
        this.snProperties = snProperties;
    }

    @Override
    public Long convert(String source) {
        if(BoolUtil.isEmpty(source)){
            return null;
        }
        Long target = ExceptionUtil.sandbox(() -> Long.valueOf(source));
        if(target != null){
            return target;
        }
        return ExceptionUtil.sandbox(() -> Long.valueOf(SnUtil.decode(source, snProperties.getSecretKey())));
    }
}
