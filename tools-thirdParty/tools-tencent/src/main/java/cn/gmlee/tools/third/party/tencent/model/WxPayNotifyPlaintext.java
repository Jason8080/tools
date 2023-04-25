package cn.gmlee.tools.third.party.tencent.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * 支付结果通知铭文.
 *
 * @author Jas °
 */
@Data
public class WxPayNotifyPlaintext implements Serializable {
    /**
     * 应用编号
     */
    private String appid;
    /**
     * 商户编号
     */
    private String mchid;
    /**
     * 商户订单号
     */
    private String out_trade_no;
    /**
     * 微信交易编号/支付订单号
     */
    private String transaction_id;
    /**
     * 交易类型，枚举值：
     * JSAPI：公众号支付
     * NATIVE：扫码支付
     * APP：APP支付
     * MICROPAY：付款码支付
     * MWEB：H5支付
     * FACEPAY：刷脸支付
     * 示例值：MICROPAY
     */
    private String trade_type;
    /**
     * 交易状态，枚举值：
     * SUCCESS：支付成功
     * REFUND：转入退款
     * NOTPAY：未支付
     * CLOSED：已关闭
     * REVOKED：已撤销（付款码支付）
     * USERPAYING：用户支付中（付款码支付）
     * PAYERROR：支付失败(其他原因，如银行返回失败)
     * 示例值：SUCCESS
     */
    private String trade_state;
    /**
     * 交易状态描述: 支付失败，请重新下单支付
     */
    private String trade_state_desc;
    /**
     * 付款银行: 银行类型，采用字符串类型的银行标识。
     * 示例值：CMC
     */
    private String bank_type;
    /**
     * 附加数据: 附加数据，在查询API和支付通知中原样返回，可作为自定义参数使用
     */
    private String attach;
    /**
     * 支付成功时间: 2018-06-08T10:34:56+08:00
     */
    private String success_time;
    /**
     * 支付人
     */
    private Payer payer;
    /**
     * 订单金额信息
     */
    private AmountInfo amount;
    /**
     * 支付场景信息
     */
    private SceneInfo scene_info;
    /**
     * 优惠功能
     */
    private List<PromotionDetail> promotion_detail;

    /**
     * The type Payer.
     */
    @Data
    public static class Payer implements Serializable {
        private String openid;
    }

    /**
     * The type Amount info.
     */
    @Data
    public static class AmountInfo implements Serializable {
        /**
         * 总金额: 分
         */
        private Long total;
        /**
         * 币种: CNY
         */
        private String currency;
        /**
         * 用户支付总金额: 分
         */
        private Long payer_total;
        /**
         * 用户支付币种: CNY
         */
        private String payer_currency;

    }

    /**
     * The type Scene info.
     */
    @Data
    public static class SceneInfo implements Serializable {
        /**
         * 商户终端设备号: POS1:1
         */
        private String device_id;

    }

    /**
     * The type Promotion detail.
     */
    @Data
    public static class PromotionDetail implements Serializable {

        /**
         * 券编号
         */
        private String coupon_id;
        /**
         * 券名称
         */
        private String name;
        /**
         * 优惠范围:
         * GLOBAL：全场代金券
         * SINGLE：单品优惠
         * 示例值：GLOBAL
         */
        private String scope;
        /**
         * 优惠类型:
         * CASH：充值
         * NOCASH：预充值
         * 示例值：CASH
         */
        private String type;
        /**
         * 优惠券面额
         */
        private Long amount;
        /**
         * 活动编号
         */
        private String stock_id;
        /**
         * 微信出资: 分
         */
        private Long wechatpay_contribute;
        /**
         * 商户出资: 分
         */
        private Long merchant_contribute;
        /**
         * 其他出资: 分
         */
        private Long other_contribute;
        /**
         * 优惠币种
         */
        private String currency;

        /**
         * 单品列表信息
         */
        private List<GoodsDetail> goods_detail;
    }

    /**
     * The type Goods detail.
     */
    @Data
    public static class GoodsDetail implements Serializable {
        /**
         * 商品编码
         */
        private String goods_id;
        /**
         * 购买数量
         */
        private Integer quantity;
        /**
         * 商品单价: 分
         */
        private Long unit_price;
        /**
         * 商品优惠金额
         */
        private Long discount_amount;
        /**
         * 商品备注信息
         */
        private String goods_remark;
    }
}
