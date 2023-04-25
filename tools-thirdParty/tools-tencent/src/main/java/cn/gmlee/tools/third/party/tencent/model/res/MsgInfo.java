package cn.gmlee.tools.third.party.tencent.model.res;

import lombok.Data;

/**
 * 服务号模板消息发送响应信息实体
 *
 * @author Jas °
 * @date 2021 /2/22 (周一)
 */
@Data
public class MsgInfo extends ErrorInfo {
    private Long msgid;

    @Override
    public String toString() {
        return "MsgInfo{" +
                "msgid=" + msgid +
                "} " + super.toString();
    }
}
