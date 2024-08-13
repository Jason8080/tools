package cn.gmlee.tools.jackson.codec;

import cn.gmlee.tools.base.define.RsaCodec;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.jackson.anno.Codec;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;

import java.io.IOException;

public class RsaCodecJsonSerializer extends JsonSerializer<String> implements RsaCodec, ContextualSerializer {

    private String appId;

    @Override
    public String getPrivateKey() {
        String appId = BoolUtil.notEmpty(this.appId) ? "." + this.appId : "";
        String key = String.format("tools.jackson.codec%s.privateKey", appId);
        return System.getProperty(key);
    }

    @Override
    public String getPublicKey() {
        String appId = BoolUtil.notEmpty(this.appId) ? "." + this.appId : "";
        String key = String.format("tools.jackson.codec%s.publicKey", appId);
        return System.getProperty(key);
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
                this.appId = codec.appId();
                return this;
            }
        }
        return new StringSerializer();
    }
}
