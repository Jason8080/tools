package cn.gmlee.tools.jackson.anno;

import cn.gmlee.tools.jackson.codec.RsaCodecJsonDeserializer;
import cn.gmlee.tools.jackson.codec.RsaCodecJsonSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Codec.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = RsaCodecJsonSerializer.class)
@JsonDeserialize(using = RsaCodecJsonDeserializer.class)
public @interface Codec {
    /**
     * 区分不同的应用秘钥.
     *
     * @return the string
     */
    String appId() default "";
}