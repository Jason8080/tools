package cn.gmlee.tools.third.party.alibaba.model.res;

import cn.gmlee.tools.base.define.Reflect;
import lombok.Data;

/**
 * @author Jas°
 * @date 2021/3/9 (周二)
 */
@Data
public class AliPayResultNotifyResponse implements Reflect {
    private String return_msg;

    public AliPayResultNotifyResponse() {
    }

    public AliPayResultNotifyResponse(String return_msg) {
        this.return_msg = return_msg;
    }
}
