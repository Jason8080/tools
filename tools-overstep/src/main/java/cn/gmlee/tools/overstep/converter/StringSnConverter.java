package cn.gmlee.tools.overstep.converter;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.base.util.SnUtil;
import cn.gmlee.tools.overstep.config.SnProperties;
import org.springframework.core.convert.converter.Converter;

/**
 * .
 *
 * @author Jas°
 * @date 2021/7/28 (周三)
 */
public class StringSnConverter implements Converter<String, String> {
    private final SnProperties snProperties;

    public StringSnConverter(SnProperties snProperties) {
        this.snProperties = snProperties;
    }

    @Override
    public String convert(String source) {
        if (BoolUtil.isEmpty(source)) {
            return source;
        }
        String target = ExceptionUtil.sandbox(() -> SnUtil.decode(source, snProperties.getSecretKey()));
        return NullUtil.get(target, source);
    }
}
