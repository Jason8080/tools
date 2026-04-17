package cn.gmlee.tools.base.jackson;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.text.DateFormat;
import java.util.Date;

/**
 * The type Time json deserializer.
 */
public class TimeJsonDeserializer extends ValueDeserializer<Date> {
    /**
     * The constant instance.
     */
    public static ValueDeserializer<? extends Date> instance = new TimeJsonDeserializer();

    @Override
    public Date deserialize(JsonParser p, DeserializationContext txt) throws JacksonException {
        // 如果为空则不处理
        String text = p.getString();
        if (BoolUtil.isEmpty(text)) {
            return null;
        }
        try {
            // 优先使用配置
            DeserializationConfig config = txt.getConfig();
            DateFormat dateFormat = config.getDateFormat();
            return dateFormat.parse(text);
        } catch (Exception e) {
            return TimeUtil.parseTime(text);
        }
    }
}
