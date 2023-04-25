package cn.gmlee.tools.third.party.tencent.model.res;


import cn.gmlee.tools.base.define.Reflect;
import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * 支付结果通知相应.
 *
 * @author Jas °
 */
@Data
@XmlRootElement(name = "xml")
public class XmlWxPayResultNotifyResponse implements Reflect {
    /**
     * 响应码: SUCCESS or Other
     */
    private String return_code;
    /**
     * 错误信息: 有错误则写, 没有即空
     */
    private String return_msg;

    public XmlWxPayResultNotifyResponse() {
    }

    /**
     * Instantiates a new Pay notify response.
     *
     * @param return_code the return code
     * @param return_msg  the return msg
     */
    public XmlWxPayResultNotifyResponse(String return_code, String return_msg) {
        this.return_code = return_code;
        this.return_msg = return_msg;
    }
}
