package cn.gmlee.tools.base.define;

import cn.gmlee.tools.base.util.RsaUtil;

/**
 * RSA 编解码.
 */
public interface RsaCodec extends Codec<String,String> {
    @Override
    default Class<String> support() {
        return String.class;
    }

    @Override
    default String encode(String plaintext) {
        return RsaUtil.privateEncoder(plaintext, getPrivateKey());
    }

    @Override
    default String decode(String ciphertext) {
        return RsaUtil.publicDecoder(ciphertext, getPublicKey());
    }

    /**
     * 私钥.
     *
     * @return the private key
     */
    String getPrivateKey();

    /**
     * 公钥.
     *
     * @return the public key
     */
    String getPublicKey();
}
