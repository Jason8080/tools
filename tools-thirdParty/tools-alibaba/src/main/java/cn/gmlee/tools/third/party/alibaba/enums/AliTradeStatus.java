package cn.gmlee.tools.third.party.alibaba.enums;

/**
 * @author Jas°
 * <p>
 * 交易状态：
 * WAIT_BUYER_PAY（交易创建，等待买家付款）、
 * TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）、
 * TRADE_SUCCESS（交易支付成功）、
 * TRADE_FINISHED（交易结束，不可退款）
 * </p>
 *
 * @date 2021/3/12 (周五)
 */
public enum AliTradeStatus {
    WAIT_BUYER_PAY,
    TRADE_CLOSED,
    TRADE_SUCCESS,
    TRADE_FINISHED,
    ;
}
