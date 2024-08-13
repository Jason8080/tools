package cn.gmlee.tools.base.define;

/**
 * 编解码.
 *
 * <p>基于Jackson序列化应实现{@link cn.gmlee.tools.jackson.codec.RsaCodecJsonSerializer}</p>
 * <p>基于Jackson反序列化应实现{@link cn.gmlee.tools.jackson.codec.RsaCodecJsonDeserializer}</p>
 *
 * @param <P> the type parameter
 * @param <C> the type parameter
 */
public interface Codec<P,C> {
    /**
     * 支持的类型.
     *
     * @return the class
     */
    Class<P> support();

    /**
     * 编码.
     *
     * @param plaintext the plaintext
     * @return the string
     */
    C encode(P plaintext);

    /**
     * 解码.
     *
     * @param ciphertext the ciphertext
     * @return the string
     */
    C decode(C ciphertext);
}
