package cn.gmlee.tools.base.mod;

import cn.gmlee.tools.base.define.Reflect;
import cn.gmlee.tools.base.util.TimeUtil;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 通用接口签名实体
 *
 * @author Jas°
 * @date 2021/3/10 (周三)
 */
@Data
public class Sign implements Reflect {
    @NotEmpty(message = "应用编号是空")
    protected String appId;
    @NotEmpty(message = "随机码是空")
    protected String nonce;
    @NotEmpty(message = "时间戳是空")
    protected Long timestamp;
    @NotEmpty(message = "签名是空")
    protected String signature;

    public Sign() {
    }

    public Sign(@NotEmpty(message = "应用编号是空") String appId) {
        this.appId = appId;
        this.nonce = TimeUtil.getCurrentMs();
        this.timestamp = System.currentTimeMillis() + (3600 * 1000);
    }

    public Sign(@NotEmpty(message = "应用编号是空") String appId, @NotEmpty(message = "时间戳是空") Long timestamp) {
        this.appId = appId;
        this.nonce = TimeUtil.getCurrentMs();
        this.timestamp = timestamp;
    }

    public Sign(@NotEmpty(message = "应用编号是空") String appId, @NotEmpty(message = "随机码是空") String nonce, @NotEmpty(message = "时间戳是空") Long timestamp) {
        this.appId = appId;
        this.nonce = nonce;
        this.timestamp = timestamp;
    }

    public Sign(@NotEmpty(message = "应用编号是空") String appId, @NotEmpty(message = "随机码是空") String nonce, @NotEmpty(message = "时间戳是空") Long timestamp, @NotEmpty(message = "签名是空") String signature) {
        this.appId = appId;
        this.nonce = nonce;
        this.timestamp = timestamp;
        this.signature = signature;
    }
}
