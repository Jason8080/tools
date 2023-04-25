package cn.gmlee.tools.third.party.tencent.model.res;


import lombok.Data;

import java.io.Serializable;

/**
 * 支付结果通知相应.
 * @author Jas°
 */
@Data
public class PayResultNotifyResponse implements Serializable {
    /**
     * 响应码: SUCCESS or Other
     */
    private String code;
    /**
     * 错误信息: 有错误则写, 没有即空
     */
    private String message;

    /**
     * Instantiates a new Pay notify response.
     *
     * @param code    the code
     * @param message the message
     */
    public PayResultNotifyResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
