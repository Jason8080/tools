package cn.gmlee.tools.jackson.codec;

import cn.gmlee.tools.base.define.RsaCodec;
import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.jackson.anno.Codec;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.jdk.StringSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RsaCodecJsonSerializer extends ValueSerializer<String> implements RsaCodec {

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
    public void serialize(String value, JsonGenerator gen, SerializationContext s) throws JacksonException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(ExceptionUtil.sandbox(() -> encode(value), e -> value));
    }

    @Override
    @SuppressWarnings("all")
    public ValueSerializer<?> createContextual(SerializationContext prov, BeanProperty property) throws DatabindException {
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
        return StringSerializer.instance;
    }

    private ValueSerializer<?> that(Codec codec) {
        this.appId = codec.appId();
        return this;
    }
}
