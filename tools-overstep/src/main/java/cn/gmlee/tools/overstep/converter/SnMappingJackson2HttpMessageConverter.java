package cn.gmlee.tools.overstep.converter;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.base.util.SnUtil;
import cn.gmlee.tools.overstep.config.SnProperties;
import cn.gmlee.tools.overstep.kit.ExcludeKit;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;

/**
 * .
 *
 * @author Jas°
 * @date 2021/7/28 (周三)
 */
public class SnMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

    private final SnProperties snProperties;

    public SnMappingJackson2HttpMessageConverter(ObjectMapper objectMapper, SnProperties snProperties) {
        super(objectMapper);
        this.snProperties = snProperties;
        SimpleModule typeModule = new SimpleModule();
        typeModule.addSerializer(String.class, stringSnSerializer());
        typeModule.addDeserializer(String.class, stringSnDeserializer());
        typeModule.addSerializer(Long.class, longSnSerializer());
        typeModule.addDeserializer(Long.class, longSnDeserializer());
        objectMapper.registerModule(typeModule);
    }

    private StdScalarSerializer<String> stringSnSerializer() {
        return new StdScalarSerializer<String>(String.class) {
            @Override
            public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                String name = ExceptionUtil.sandbox(() -> gen.getOutputContext().getCurrentName());
                if (ExcludeKit.check(snProperties, name)) {
                    String sn = ExceptionUtil.sandbox(() -> SnUtil.encode(value, snProperties.getSecretKey()));
                    if (sn != null) {
                        gen.writeString(sn);
                        return;
                    }
                }
                gen.writeString(value);
            }
        };
    }

    private StdScalarDeserializer<String> stringSnDeserializer() {
        return new StdScalarDeserializer<String>(String.class) {
            @Override
            public String deserialize(JsonParser p, DeserializationContext cx) throws IOException, JsonProcessingException {
                String name = p.getCurrentName();
                if (ExcludeKit.exist(snProperties, name)) {
                    String id = ExceptionUtil.sandbox(() -> SnUtil.decode(p.getValueAsString(), snProperties.getSecretKey()));
                    QuickUtil.isFalse(snProperties.getAllowPlaintext(), () -> AssertUtil.notNull(id, String.format("非法参数: %s", p.getValueAsString())));
                    if (id != null) {
                        return id;
                    }
                }
                return p.getValueAsString();
            }
        };
    }

    private StdScalarSerializer<Long> longSnSerializer() {
        return new StdScalarSerializer<Long>(Long.class) {
            @Override
            public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                String name = ExceptionUtil.sandbox(() -> gen.getOutputContext().getCurrentName());
                if (ExcludeKit.check(snProperties, name)) {
                    String sn = ExceptionUtil.sandbox(() -> SnUtil.encode(value.toString(), snProperties.getSecretKey()));
                    if (sn != null) {
                        gen.writeString(sn);
                        return;
                    }
                }
                gen.writeNumber(value);
            }
        };
    }

    private StdScalarDeserializer<Long> longSnDeserializer() {
        return new StdScalarDeserializer<Long>(Long.class) {
            @Override
            public Long deserialize(JsonParser p, DeserializationContext cx) throws IOException, JsonProcessingException {
                String name = p.getCurrentName();
                if (ExcludeKit.exist(snProperties, name)) {
                    Long id = ExceptionUtil.sandbox(() -> Long.valueOf(SnUtil.decode(p.getValueAsString(), snProperties.getSecretKey())));
                    QuickUtil.isFalse(snProperties.getAllowPlaintext(), () -> AssertUtil.notNull(id, String.format("非法参数: %s", p.getValueAsString())));
                    if (id != null) {
                        return id;
                    }
                }
                return p.getValueAsLong();
            }
        };
    }
}
