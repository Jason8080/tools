package cn.gmlee.tools.third.party.tencent.model.req;

import lombok.Data;

/**
 * 发送模板消息(TM)的请求参数实体
 *
 * @author Jas°
 * @date 2020/12/8 (周二)
 */
@Data
public class SendServiceSuRequest extends SendServiceTmRequest {
    /**
     * 订阅场景值
     */
    private String scene;
    /**
     * 消息标题，15字以内
     */
    private String title;
}
