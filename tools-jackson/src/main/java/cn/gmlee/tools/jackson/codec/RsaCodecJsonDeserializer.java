package cn.gmlee.tools.jackson.codec;

import cn.gmlee.tools.base.define.RsaCodec;
import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.jackson.anno.Codec;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.jdk.StringDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RsaCodecJsonDeserializer extends ValueDeserializer<String> implements RsaCodec {

    private String appId;

    public RsaCodecJsonDeserializer() {
        this.appId = null;
    }

    public RsaCodecJsonDeserializer(String appId) {
        this.appId = appId;
    }

    @Override
    public String getAppId() {
        return this.appId;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext c) throws JacksonException {
        String text = p.getString();
        if (text == null) {
            return null;
        }
        return ExceptionUtil.sandbox(() -> decode(text), e -> text);
    }

    @Override
    @SuppressWarnings("all")
    public ValueDeserializer<?> createContextual(DeserializationContext c, BeanProperty property) throws DatabindException {
        if (property != null) {
            Codec codec = property.getAnnotation(Codec.class);
            if (codec == null) {
                codec = property.getContextAnnotation(Codec.class);
            }
            if (codec != null) {
                boolean isCharSequence = BoolUtil.eq(String.class, property.getType().getRawClass());
                String msg = String.format("@Codec (%s)%s is not string!", property.getType().getRawClass().getSimpleName(), property.getName());
                QuickUtil.isFalse(isCharSequence, () -> log.error("解码异常", new SkillException(XCode.FAIL.code, msg)));
                return this.that(codec);
            }
        }
        return StringDeserializer.instance;
    }

    private ValueDeserializer<?> that(Codec codec) {
        this.appId = codec.appId();
        return this;
    }
}
