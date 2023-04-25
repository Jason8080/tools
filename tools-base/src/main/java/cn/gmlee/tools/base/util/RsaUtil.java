package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.mod.Sign;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * Rsa加密解密工具
 * 用公钥加密, 私钥解密
 * 已实现: 分段加密解密: 不受明文长度限制
 * 模具大小: n == size (密钥大小)
 * <p>
 * RSA加密解密 此工具类能使用指定的字符串，每次生成相同的公钥和私钥且在linux和windows密钥也相同；
 * 相同的原文和密钥生成的密文相同
 *
 * @author Jas °
 * @date 2021 /3/16 (周二)
 */
public class RsaUtil {
    /**
     * The constant RSA_ALGORITHM.
     */
    public static final String RSA = "RSA";

    /**
     * 直接采用: RSA2签名方案, 不使用SHA1WithRSA(RSA1/RSA)
     */
    private static final String SIGN_SHA256RSA_ALGORITHMS = "SHA256WithRSA";

    /**
     * 生成1024大小的密钥对.
     *
     * @return the kv
     */
    public static Kv<String, String> generateSecretKey() {
        return generateSecretKey(1024);
    }

    /**
     * 生成指定大小的密钥.
     *
     * @param size the key size
     * @return the kv
     */
    public static Kv<String, String> generateSecretKey(int size) {
        AssertUtil.gte(size, 512, String.format("密钥大小低于1024存在有安全问题"));
        AssertUtil.eq(size % 8, 0, String.format("密钥大小必须是8的倍数"));
        //为RSA算法创建一个KeyPairGenerator对象
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance(RSA);
        } catch (NoSuchAlgorithmException e) {
            return ExceptionUtil.cast(e);
        }
        //初始化KeyPairGenerator对象,密钥长度
        kpg.initialize(size);
        //生成密匙对
        KeyPair keyPair = kpg.generateKeyPair();
        //得到公钥
        Key publicKey = keyPair.getPublic();
        String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        //得到私钥
        Key privateKey = keyPair.getPrivate();
        String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        return new Kv(publicKeyStr, privateKeyStr);
    }

    private static RSAPublicKey getPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //通过X509编码的Key指令获得公钥对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
        RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
        return key;
    }

    private static RSAPrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //通过PKCS#8编码的Key指令获得私钥对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
        RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
        return key;
    }

    private static RSAPublicKey getPublicKey(Kv<String, String> kv) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //通过X509编码的Key指令获得公钥对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(kv.getKey()));
        RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
        return key;
    }

    private static RSAPrivateKey getPrivateKey(Kv<String, String> kv) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //通过PKCS#8编码的Key指令获得私钥对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(kv.getVal()));
        RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
        return key;
    }

    /**
     * 公钥加密
     *
     * @param data      the data
     * @param publicKey the public key
     * @return string string
     */
    public static String publicEncoder(String data, String publicKey) {
        try {
            RSAPublicKey rsaPublicKey = getPublicKey(publicKey);
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
            return Base64.getEncoder().encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(), rsaPublicKey.getModulus().bitLength()));
        } catch (Exception e) {
            return ExceptionUtil.cast(String.format("非法公钥: %s", publicKey), e);
        }
    }

    /**
     * 私钥解密
     *
     * @param data       the data
     * @param privateKey the private key
     * @return string string
     */
    public static String privateDecoder(String data, String privateKey) {
        try {
            RSAPrivateKey rsaPrivateKey = getPrivateKey(privateKey);
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.getDecoder().decode(data), rsaPrivateKey.getModulus().bitLength()));
        } catch (Exception e) {
            return ExceptionUtil.cast(String.format("非法密文: %s", data), e);
        }
    }

    /**
     * 私钥加密
     *
     * @param data       the data
     * @param privateKey the private key
     * @return string string
     */
    public static String privateEncoder(String data, String privateKey) {
        try {
            RSAPrivateKey rsaPrivateKey = getPrivateKey(privateKey);
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, rsaPrivateKey);
            return Base64.getEncoder().encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(), rsaPrivateKey.getModulus().bitLength()));
        } catch (Exception e) {
            return ExceptionUtil.cast(String.format("非法私钥: %s", privateKey), e);
        }
    }

    /**
     * 公钥解密
     *
     * @param data      the data
     * @param publicKey the public key
     * @return string string
     */
    public static String publicDecoder(String data, String publicKey) {
        try {
            RSAPublicKey rsaPublicKey = getPublicKey(publicKey);
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.getDecoder().decode(data), rsaPublicKey.getModulus().bitLength()));
        } catch (Exception e) {
            return ExceptionUtil.cast(String.format("非法密文: %s", data), e);
        }
    }

    private static byte[] rsaSplitCodec(Cipher cipher, int opMode, byte[] bytes, int keySize) {
        int maxBlock = 0;
        if (opMode == Cipher.DECRYPT_MODE) {
            maxBlock = keySize / 8;
        } else {
            maxBlock = keySize / 8 - 11;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] buff;
        int i = 0;
        try {
            while (bytes.length > offSet) {
                if (bytes.length - offSet > maxBlock) {
                    buff = cipher.doFinal(bytes, offSet, maxBlock);
                } else {
                    buff = cipher.doFinal(bytes, offSet, bytes.length - offSet);
                }
                out.write(buff, 0, buff.length);
                i++;
                offSet = i * maxBlock;
            }
        } catch (Exception e) {
            throw new RuntimeException("加解密阀值为[" + maxBlock + "]的数据时发生异常", e);
        }
        byte[] result = out.toByteArray();
        try {
            if (out != null) {
                out.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
        return result;
    }


    // =================================================================================================================

    /**
     * Sign string.
     *
     * @param sign       the sign
     * @param privateKey the private key
     * @return the string
     */
    public static String sign(Sign sign, String privateKey) {
        String content = getContent(sign);
        try {
            RSAPrivateKey rsaPrivateKey = getPrivateKey(privateKey);
            Signature signature = Signature.getInstance(SIGN_SHA256RSA_ALGORITHMS);
            signature.initSign(rsaPrivateKey);
            signature.update(content.getBytes());
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            return ExceptionUtil.cast(String.format("签名失败: %s", content), e);
        }
    }


    /**
     * Check boolean.
     *
     * @param sign      the sign
     * @param publicKey the public key
     * @return the boolean
     */
    public static boolean check(Sign sign, String publicKey) {
        String content = getContent(sign);
        try {
            RSAPublicKey rsaPublicKey = getPublicKey(publicKey);
            Signature signature = Signature.getInstance(SIGN_SHA256RSA_ALGORITHMS);
            signature.initVerify(rsaPublicKey);
            signature.update(content.getBytes());
            return signature.verify(Base64.getDecoder().decode(sign.getSignature()));
        } catch (Exception e) {
            return ExceptionUtil.cast(String.format("验签失败: %s", content), e);
        }
    }

    private static String getContent(Sign sign) {
        Map<String, Object> map = ClassUtil.generateMap(sign);
        map.remove(SignUtil.getSignature());
        TreeMap<String, Object> treeMap = CollectionUtil.keySort(map);
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, Object>> it = treeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> next = it.next();
            Object os = next.getValue();
            String toString = os.toString();
            if (os instanceof Object[]) {
                toString = Arrays.toString((Object[]) os);
            }
            sb.append(toString);
        }
        try {
            // 因为验签是不需要解密查看内容的所以直接不可逆加密
            return Md5Util.encode(sb.toString());
        } catch (Exception e) {
            throw new SkillException(XCode.CONSENSUS_SIGN2001.code, e);
        }
    }


}
