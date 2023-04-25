package cn.gmlee.tools.jackson.assist;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * The type Time json deserializer.
 */
public class TimeJsonDeserializer extends JsonDeserializer<Date> {
    /**
     * The constant instance.
     */
    public static JsonDeserializer<? extends Date> instance = new TimeJsonDeserializer();

    @Override
    public Date deserialize(JsonParser p, DeserializationContext txt) throws IOException, JsonProcessingException {
        // 如果是空则不处理
        String text = p.getText();
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
