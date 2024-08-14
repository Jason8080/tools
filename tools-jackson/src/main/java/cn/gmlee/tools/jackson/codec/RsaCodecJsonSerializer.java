package cn.gmlee.tools.jackson.codec;

import cn.gmlee.tools.base.define.RsaCodec;
import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.jackson.anno.Codec;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class RsaCodecJsonSerializer extends JsonSerializer<String> implements RsaCodec, ContextualSerializer {

    private String appId;

    public RsaCodecJsonSerializer() {
        this.appId = null;
    }

    public RsaCodecJsonSerializer(String appId) {
        this.appId = appId;
    }

    @Override
    public String getAppId() {
        return this.appId;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider s) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(ExceptionUtil.sandbox(() -> encode(value), value));
    }

    @Override
    @SuppressWarnings("all")
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            Codec codec = property.getAnnotation(Codec.class);
            if (codec == null) {
                codec = property.getContextAnnotation(Codec.class);
            }
            if (codec != null) {
                boolean isCharSequence = BoolUtil.eq(String.class, property.getType().getRawClass());
                String msg = String.format("@Codec (%s)%s is not string!", property.getType().getRawClass().getSimpleName(), property.getName());
                QuickUtil.isFalse(isCharSequence, () -> log.error("编码异常", new SkillException(XCode.FAIL.code, msg)));
                return this.that(codec);
            }
        }
        return new StringSerializer();
    }

    private JsonSerializer<?> that(Codec codec) {
        this.appId = codec.appId();
        return this;
    }
}
