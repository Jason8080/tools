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
public class SnIntegerConverter implements Converter<String, Integer> {
    private final SnProperties snProperties;

    public SnIntegerConverter(SnProperties snProperties) {
        this.snProperties = snProperties;
    }

    @Override
    public Integer convert(String source) {
        if(BoolUtil.isEmpty(source)){
            return null;
        }
        Integer target = ExceptionUtil.sandbox(() -> Integer.valueOf(source));
        if(target != null){
            return target;
        }
        return ExceptionUtil.sandbox(() -> Integer.valueOf(SnUtil.decode(source, snProperties.getSecretKey())));
    }
}
