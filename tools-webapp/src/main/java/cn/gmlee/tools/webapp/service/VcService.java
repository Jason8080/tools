package cn.gmlee.tools.webapp.service;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.VcException;
import cn.gmlee.tools.base.mod.JsonResult;
import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.IdUtil;
import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.base.util.VcUtil;
import cn.gmlee.tools.redis.util.RedisClient;
import cn.gmlee.tools.webapp.config.vc.VcProperties;

/**
 * 验证码服务.
 *
 * @author Jas
 */
public class VcService {

    private RedisClient<String, String> rcVc;

    private RedisClient<String, Integer> rcCount;

    private VcProperties vcProperties;

    /**
     * Gets vc properties.
     *
     * @return the vc properties
     */
    public VcProperties getVcProperties() {
        return vcProperties;
    }

    /**
     * Instantiates a new Vc service.
     *
     * @param rc           the rc
     * @param vcProperties the vc properties
     */
    public VcService(RedisClient rc, VcProperties vcProperties) {
        this.rcVc = rc;
        this.rcCount = rc;
        this.vcProperties = vcProperties;
    }

    /**
     * 要求{@param key}的业务数据必须输入验证码.
     * <p>
     * 在业务代码中需要累计异常次数的位置加入这行代码;
     * 它可以自动累计调用次数, 配合check使用可以达到指定次数后必须输入验证码;
     * </p>
     *
     * @param key the key
     * @return the boolean
     */
    public boolean ask(String key) {
        Integer count = NullUtil.get(rcCount.get(vcProperties.getObservationRedisKey().concat(key)), 0);
        rcCount.set(vcProperties.getObservationRedisKey().concat(key), ++count, vcProperties.getObservationExpire());
        return BoolUtil.gt(count, vcProperties.getObservationCount());
    }

    /**
     * 释放{@param key}的业务数据不再要求必须输入验证码.
     * <p>
     * 如果它已经改过自新可以在代码中调用该方法赦免一次;
     * </p>
     *
     * @param key the key
     */
    public void release(String key) {
        rcCount.delete(vcProperties.getObservationRedisKey().concat(key));
    }

    /**
     * 检查验证码是否通过检查.
     * <p>
     * 在业务代码靠前的位置加入该项检查.
     * </p>
     *
     * @param key the key
     * @return the boolean
     */
    public void check(String key) {
        if (!isOk(key)) {
            throw new VcException(key);
        }
    }

    /**
     * 检查是否需要验证码以及验证码是否通过检查.
     *
     * @param key the key
     * @return the boolean
     */
    private boolean isOk(String key) {
        if (required(key)) {
            return VcUtil.isOk();
        }
        return true;
    }

    /**
     * 检测是否需要输入验证码.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean required(String key) {
        Integer count = NullUtil.get(rcCount.get(vcProperties.getObservationRedisKey().concat(key)), 0);
        return BoolUtil.gt(count, vcProperties.getObservationCount());
    }

    // -----------------------------------------------------------------------------------------------------------------


    /**
     * 创建响应.
     *
     * @param e the e
     * @return the json result
     */
    public JsonResult generateJsonResult(VcException e) {
        return generateJsonResult(e.getKey());
    }

    /**
     * 创建响应.
     *
     * @param key the key
     * @return the json result
     */
    public JsonResult generateJsonResult(String key) {
        if (!ask(key)) {
            return new JsonResult(XCode.ACCOUNT_INCORRECT7004);
        }
        return generateVcJsonResult(XCode.CONSENSUS_VC2012.code, XCode.CONSENSUS_VC2012.msg);
    }

    /**
     * 创建响应.
     *
     * @param key  the key
     * @param code the code
     * @param msg  the msg
     * @return the json result
     */
    public JsonResult generateJsonResult(String key, Integer code, String msg) {
        if (!ask(key)) {
            return new JsonResult(XCode.ACCOUNT_INCORRECT7004);
        }
        return generateVcJsonResult(code, msg);
    }

    /**
     * Gets json result.
     *
     * @param code the code
     * @param msg  the msg
     * @return the json result
     */
    public JsonResult generateVcJsonResult(Integer code, String msg) {
        // key 存的是 vc (真实验证码)
        // val 存的是 图片 (base64)
        Kv<String, String> vc = VcUtil.generateBase64(vcProperties.getLength(), vcProperties.getWidth(), vcProperties.getHeight());
        // 存储真实验证码
        String id = setVc(IdUtil.uuidReplaceUpperCase(), vc);
        // 需要返回ID, 而不是返回真实的验证码
        vc.setKey(id);
        return new JsonResult(code, msg, vc);
    }

    /**
     * Gets vc.
     *
     * @param id the id
     * @return the vc
     */
    public String getVc(String id) {
        return rcVc.get(vcProperties.getRedisKey().concat(id));
    }

    private String setVc(String id, Kv<String, String> vc) {
        if (!rcVc.setNx(vcProperties.getRedisKey().concat(id), vc.getKey(), vcProperties.getVcExpire())) {
            return setVc(IdUtil.uuidReplaceUpperCase(), vc);
        }
        return id;
    }
}
