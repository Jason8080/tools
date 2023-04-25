package cn.gmlee.tools.third.party.tencent.model.req;


import lombok.Data;

import java.io.Serializable;

/**
 * 支付结果通知请求.
 *
 * @author Jas°
 */
@Data
public class PayResultNotifyRequest implements Serializable {
    /**
     * 通知编号: EV-2018022511223320873
     */
    private String id;
    /**
     * 创建时间: 2015-05-20T13:29:35+08:00
     */
    private String create_time;
    /**
     * 通知类型: TRANSACTION.SUCCESS
     */
    private String event_type;
    /**
     * 通知数据类型: encrypt-resource
     */
    private String resource_type;
    /**
     * 回调摘要: 支付成功
     */
    private String summary;
    /**
     * 通知数据
     */
    private Resource resource;

    /**
     * The type Resource.
     */
    @Data
    public static class Resource implements Serializable {
        /**
         * 加密算法类型: AEAD_AES_256_GCM
         */
        private String algorithm;
        /**
         * 密文内容
         */
        private String ciphertext;
        /**
         * 附加数据
         */
        private String associated_data;
        /**
         * 加密时用的随机串
         */
        private String nonce;
        /**
         * 原始回调类型: transaction
         */
        private String original_type;
    }
}
