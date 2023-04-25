package cn.gmlee.tools.base.enums;

/**
 * 通用的状态码枚举
 *
 * @author Jas°
 * @date 2021/1/11 (周一)
 */
public enum XState {
    // -9destroy  -8recycle -7delete -6history -5freeze -4expire -3trim -2reset -1cancel
    // 0 no
    // 1 yes 2skip 3wait 4underway 5confirm 6finish 7close 8rollback 9override
    DESTROY(-9),
    RECYCLE(-8),
    DELETE(-7),
    HISTORY(-6),
    FREEZE(-5),
    EXPIRE(-4),
    TRIM(-3),
    RESET(-2),
    CANCEL(-1),
    NO(0),
    YES(1),
    SKIP(2),
    WAIT(3),
    UNDERWAY(4),
    CONFIRM(5),
    FINISH(6),
    CLOSE(7),
    ROLLBACK(8),
    OVERRIDE(9),
    // 10: 已提交, 20: 已支付, 30: 已接单, 40: 已发货, 50: 已收货, 70: 已完成, 80: 已打款, 90: 已结束
    SUBMITTED(10),
    PAID(20),
    ACCEPTED(30),
    SEND(40),
    RECEIVED(50),
    FINISHED(70),
    REMITTANCE(80),
    END(90),
    // 60: 退货-已申请, 61: 退货-已同意, 62: 退货-已发货, 63: 退货-已收货, 64: 退货-已完成, 65: 退货-已退款
    ROLLBACK_APPLIED(60),
    ROLLBACK_AGREED(61),
    ROLLBACK_SEND(62),
    ROLLBACK_RECEIVED(63),
    ROLLBACK_FINISHED(64),
    ROLLBACK_REMITTANCE(65),
    // 100: ...
    ;
    public final int code;

    XState(int code) {
        this.code = code;
    }
}
