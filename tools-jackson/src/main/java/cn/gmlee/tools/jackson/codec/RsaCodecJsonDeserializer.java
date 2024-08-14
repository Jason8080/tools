package cn.gmlee.tools.jackson.codec;

import cn.gmlee.tools.base.define.RsaCodec;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.jackson.anno.Codec;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

import java.io.IOException;

public class RsaCodecJsonDeserializer extends JsonDeserializer<String> implements RsaCodec, ContextualDeserializer {

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
    public String deserialize(JsonParser p, DeserializationContext c) throws IOException {
        if (p.getText() == null) {
            return null;
        }
        return ExceptionUtil.sandbox(() -> decode(p.getText()), p.getText());
    }

    @Override
    @SuppressWarnings("all")
    public JsonDeserializer<?> createContextual(DeserializationContext c, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            Codec codec = property.getAnnotation(Codec.class);
            if (codec == null) {
                codec = property.getContextAnnotation(Codec.class);
            }
            if (codec != null) {
                return this.that(codec);
            }
        }
        return new StringDeserializer();
    }

    private JsonDeserializer<?> that(Codec codec) {
        this.appId = codec.appId();
        return this;
    }
}
