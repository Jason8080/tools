package cn.gmlee.tools.third.party.tencent.model.req;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 发送模板消息(TM)的请求参数实体
 *
 * @author Jas°
 * @date 2020/12/8 (周二)
 */
@Data
public class SendServiceTmRequest implements Serializable {
    /**
     * 接收人OpenId
     */
    private String touser;
    /**
     * 模板编号
     */
    private String template_id;
    /**
     * 模板跳转链接（海外帐号没有跳转能力）
     */
    private String url;
    /**
     * 跳小程序所需数据，不需跳小程序可不用传该数据
     */
    private Miniprogram miniprogram;
    private Map<String, Text> data;


    @Data
    public static class Miniprogram implements Serializable {
        /**
         * "appid":"xiaochengxuappid12345"
         */
        private String appid;
        /**
         * "pagepath":"index?foo=bar"
         */
        private String pagepath;
    }


    @Data
    public static class Text implements Serializable {
        /**
         * "value":"欢迎再次购买！"
         */
        private String value;
        /**
         * "color":"#173177"
         */
        private String color;

        public Text(String value) {
            this.value = value;
        }
    }
}
