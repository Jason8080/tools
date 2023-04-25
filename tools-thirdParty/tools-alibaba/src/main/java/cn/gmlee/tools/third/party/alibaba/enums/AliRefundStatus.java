package cn.gmlee.tools.third.party.alibaba.enums;

/**
 * <p>
 *     只在使用异步退款接口情况下才返回该字段。
 *     REFUND_PROCESSING 退款处理中；
 *     REFUND_SUCCESS 退款处理成功；
 *     REFUND_FAIL 退款失败;
 * </p>
 *
 * @author Jas °
 * @date 2021 /6/1 (周二)
 */
public enum AliRefundStatus {
    REFUND_PROCESSING,
    REFUND_SUCCESS,
    REFUND_FAIL,
    ;
}
