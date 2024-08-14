package cn.gmlee.tools.base.define;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.RsaUtil;

/**
 * RSA 编解码.
 */
public interface RsaCodec extends Codec<String, String> {

    String PUBLIC_KEY = "publicKey";
    String PRIVATE_KEY = "privateKey";

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
    default String getPrivateKey() {
        return getPrivateKey(getAppKey(PRIVATE_KEY));
    }

    /**
     * 公钥.
     *
     * @return the public key
     */
    default String getPublicKey() {
        return getPublicKey(getAppKey(PUBLIC_KEY));
    }

    /**
     * Get key string.
     *
     * @param name the name
     * @return the string
     */
    default String getAppKey(String name) {
        String appId = getAppId();
        String key = BoolUtil.notEmpty(appId) ? appId : "default";
        return String.format("tools.jackson.codec.%s.%s", key, name);
    }

    /**
     * Gets private appKey.
     *
     * @param appKey the appKey
     * @return the private appKey
     */
    default String getPrivateKey(String appKey) {
        return System.getProperty(appKey);
    }

    /**
     * Gets public appKey.
     *
     * @param appKey the appKey
     * @return the public appKey
     */
    default String getPublicKey(String appKey) {
        return System.getProperty(appKey);
    }

    /**
     * Gets app id.
     *
     * @return the app id
     */
    String getAppId();
}
