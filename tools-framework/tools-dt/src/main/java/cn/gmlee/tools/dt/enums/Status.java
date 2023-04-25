package cn.gmlee.tools.dt.enums;

/**
 * 事务状态码枚举
 *
 * @author Jas°
 * @date 2021/1/11 (周一)
 */
public enum Status {
    ROLLBACK(-1, "已回滚"),
    UNDERWAY(0, "进行中"),
    COMMIT(1, "已提交"),
    ;
    public final int code;
    public final String desc;

    Status(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
