package cn.gmlee.tools.third.party.tencent.kit;

import cn.gmlee.tools.base.util.ExceptionUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 测试Wx工具类
 *
 * @author Jas °
 * @date 2020 /10/10 (周六)
 */
public class V3ApiKit {
    private static final int KEY_LENGTH_BYTE = 32;

    /**
     * 支付回调数据解密
     *
     * @param v3apiKey       the v 3 api key
     * @param associatedData the associated data
     * @param nonce          the nonce
     * @param ciphertext     the ciphertext
     * @return string
     */
    public static String decodeCiphertext(String v3apiKey, String associatedData, String nonce, String ciphertext) {
        byte[] aesKey = v3apiKey.getBytes();
        // KEY_LENGTH_BYTE = 32
        if (aesKey.length != KEY_LENGTH_BYTE) {
            throw new IllegalArgumentException("无效的ApiV3Key，长度必须为32个字节");
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
            // TAG_LENGTH_BIT = 128
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes());

            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            cipher.updateAAD(associatedData.getBytes());

            return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), "utf-8");
        } catch (Exception e) {
            return ExceptionUtil.cast(e);
        }
    }

}
