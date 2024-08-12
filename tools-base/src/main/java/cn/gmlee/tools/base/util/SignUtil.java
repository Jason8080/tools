package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.mod.Sign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 签名工具类
 *
 * @author Jas °
 * @date 2020 /8/28 (周五)
 */
public class SignUtil {
    private static final Logger logger = LoggerFactory.getLogger(SignUtil.class);
    private static final String APP_ID = "appId";
    private static final String APP_SECRET = "appSecret";
    private static final String TIMESTAMP = "timestamp";
    private static final String NONCE = "nonce";
    private static final String SIGNATURE = "signature";

    /**
     * 请求头中的参数名自动转为小写 (大写敏感).
     *
     * @return the app id
     */
    public static String getAppId() {
        return APP_ID.toLowerCase();
    }

    /**
     * 请求头中的参数名自动转为小写 (大写敏感).
     *
     * @return the app secret
     */
    public static String getAppSecret() {
        return APP_SECRET.toLowerCase();
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public static String getTimestamp() {
        return TIMESTAMP.toLowerCase();
    }

    /**
     * Gets nonce.
     *
     * @return the nonce
     */
    public static String getNonce() {
        return NONCE.toLowerCase();
    }

    /**
     * Gets signature.
     *
     * @return the signature
     */
    public static String getSignature() {
        return SIGNATURE.toLowerCase();
    }

    /**
     * 期望参与签名的请求头.
     */
    public static List<String> headers = Arrays.asList(getAppId(), getTimestamp(), getNonce());


    /**
     * 签名.
     *
     * @param <T>       the type parameter
     * @param map       参与签名的参数
     * @param secretKey the secret key
     * @return 签名 string
     * @throws Exception 签名工具异常
     */
    @Deprecated
    public static <T> String signDeprecated(Map<String, T> map, String secretKey) {
        TreeMap<String, T> treeMap = CollectionUtil.keySort(map);
        treeMap.remove(getSignature());
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, T>> it = treeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, T> next = it.next();
            T os = next.getValue();
            String toString = os != null ? os.toString() : null;
            // 非 (...) 需要拼接JSON有序序列化字符串(包括null字段)
            if (os != null && !BoolUtil.isBaseClass(os, String.class)) {
                toString = JsonUtil.toJson(os);
            }
            // 八大基本类型及其包装类型+字符串类型可直接拼接字符串
            sb.append(toString);
        }
        String concat = secretKey.concat(sb.toString()).concat(secretKey);
        try {
            return Md5Util.encode(concat);
        } catch (Exception e) {
            throw new SkillException(XCode.API_SIGN.code, e);
        }
    }

    /**
     * 签名.
     *
     * @param <T>       the type parameter
     * @param t         the t
     * @param secretKey the secret key
     * @return string
     */
    public static <T> String sign(T t, String secretKey) {
        String json = JsonUtil.toJson(t);
        logger.info("签名内容: {}", json);
        String concat = secretKey.concat(json).concat(secretKey);
        logger.info("签名字符: {}", concat);
        return ExceptionUtil.suppress(() -> Md5Util.encode(concat), x -> new SkillException(XCode.API_SIGN));
    }

    /**
     * 签名 .
     * <p>
     * sign 必须实现 reflect 接口
     * </p>
     *
     * @param sign      the sign
     * @param secretKey the secret key
     * @return string string
     */
    public static String sign(Sign sign, String secretKey) {
        Map<String, Object> map = ClassUtil.generateMap(sign);
        map.remove(getSignature());
        logger.debug("本次参与签名的参数: {}", map);
        sign.setSignature(sign(map, secretKey));
        return sign.getSignature();
    }

    /**
     * 将签名对象转成HttpClient请求头.
     *
     * @param sign the sign
     * @return kv [ ]
     */
    public static Kv<String, String>[] getHeaderKvs(Sign sign) {
        List<Kv<String, String>> kvs = new ArrayList();
        Map<String, Object> map = ClassUtil.generateMap(sign);
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> next = it.next();
            String key = next.getKey();
            Object value = next.getValue();
            // 是空不会传递
            if (BoolUtil.isBaseClass(value, String.class)) {
                kvs.add(new Kv(key, value.toString()));
            }
        }
        return kvs.toArray(new Kv[0]);
    }

    /**
     * 签名.
     *
     * @param req          参与签名的参数
     * @param secretKey    the secret key
     * @param extraHeaders the extra headers
     * @return 签名 string
     * @throws Exception the exception
     */
    public static String sign(HttpServletRequest req, String secretKey, String... extraHeaders) {
        Map<String, Object> map = new HashMap(0);
        // 防止乱码
        Map<String, Object> queryMap = WebUtil.getParams(req.getQueryString());
        map.putAll(queryMap);
        Map<String, Object> headerMap = getHeaderMap(req, extraHeaders);
        map.putAll(headerMap);
        return sign(map, secretKey);
    }

    /**
     * 用于方便打印日志
     *
     * @param req          the req
     * @param extraHeaders the extra headers
     * @return header map
     */
    public static Map<String, Object> getHeaderMap(HttpServletRequest req, String... extraHeaders) {
        Map<String, Object> map = new HashMap(3);
        Enumeration<String> names = req.getHeaderNames();
        while (names.hasMoreElements()) {
            String next = names.nextElement();
            if (next == null) {
                continue;
            }
            if (headers.contains(next.toLowerCase()) || BoolUtil.containOne(extraHeaders, next.toLowerCase())) {
                map.put(next, req.getHeader(next));
            }
        }
        return map;
    }

    /**
     * 签名验证 .
     *
     * @param req          the req
     * @param secretKey    the secret key
     * @param extraHeaders the extra headers
     * @return the boolean
     * @throws Exception the exception
     */
    public static boolean check(HttpServletRequest req, String secretKey, String... extraHeaders) {
        String sign = sign(req, secretKey, extraHeaders);
        String signature = req.getHeader(getSignature());
        boolean isOk = sign.equals(signature);
        if (!isOk) {
            logger.error("请求验签失败: {} eq {}\r\n -> 地址: {}\r\n -> 参数{}", signature, sign,
                    WebUtil.getUrl(req) + "?" + req.getQueryString(), getHeaderMap(req, extraHeaders)
            );
        }
        return isOk;
    }

    /**
     * 签名验证 .
     *
     * @param sign      the sign
     * @param secretKey the secret key
     * @return boolean boolean
     */
    public static boolean check(@Validated Sign sign, String secretKey) {
        String signature = sign(sign, secretKey);
        return Objects.equals(sign.getSignature(), signature);
    }

    /**
     * 签名验证 .
     *
     * @param map       the map
     * @param secretKey the secret key
     * @return boolean 验签结果
     */
    public static boolean check(Map<String, Object> map, String secretKey) {
        String signature = (String) map.remove(getSignature());
        String sign = sign(map, secretKey);
        logger.info("签名结果: {}", sign);
        return sign.equals(signature);
    }
}
