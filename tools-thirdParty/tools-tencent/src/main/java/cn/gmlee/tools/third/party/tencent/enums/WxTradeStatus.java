package cn.gmlee.tools.third.party.tencent.enums;

/**
 * @author Jas°
 * <p>
 * 支付状态机请见下单API页面
 * SUCCESS--支付成功
 * REFUND--转入退款
 * NOTPAY--未支付
 * CLOSED--已关闭
 * REVOKED--已撤销(刷卡支付)
 * USERPAYING--用户支付中
 * PAYERROR--支付失败(其他原因，如银行返回失败)
 * ACCEPT--已接收，等待扣款
 * </p>
 *
 * @date 2021/3/12 (周五)
 */
public enum WxTradeStatus {
    SUCCESS,
    REFUND,
    NOTPAY,
    CLOSED,
    REVOKED,
    USERPAYING,
    PAYERROR,
    ACCEPT,
    ;
}
