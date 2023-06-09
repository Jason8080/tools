package cn.gmlee.tools.base.enums;

/**
 * 通用响应码枚举.
 * msg: 展示给用户看的
 * desc: 不展示给用户看, 但开发者可以查看的
 *
 * @author Jas °
 */
public enum XCode {
    // 基本成功
    HTTP_OK(200, "请求成功"),
    // 资源丢失
    HTTP_MISSING(404, "资源丢失"),
    // 系统离线
    HTTP_OFFLINE(502, "系统离线"),
    // -----------------------------------------------------------------------------------------------------------------
    OK(1, "操作成功"), // 绿色
    OP(0, "操作不当"), // 黄色
    FAIL(-1, "操作失败"), // 红色
    // -----------------------------------------------------------------------------------------------------------------
    RETRY(1000, "请重试"), // 前端直接重新发起请求
    REDIRECT(2000, "重定向"), // 前端根据响应数据发起新的请求
    RETYPE_VC(3000, "请重新输入验证码"), // 前端直接刷新验证码
    // -----------------------------------------------------------------------------------------------------------------
    ASSERT_FAIL(FAIL.code, "断言失败"),
    // -----------------------------------------------------------------------------------------------------------------
    API_SIGN(FAIL.code, "签名不合法"),
    API_PARAM(FAIL.code, "参数不正确"),
    API_METHOD(FAIL.code, "方法不支持"),
    // -----------------------------------------------------------------------------------------------------------------
    THIRD_PARTY_FAIL(FAIL.code, "请求第三方失败"),
    // -----------------------------------------------------------------------------------------------------------------
    REQUEST_FAIL(FAIL.code, "请求失败"),
    REQUEST_FREQUENTLY(FAIL.code, "请求频繁"),
    REQUEST_CANCELLED(FAIL.code, "取消请求"),
    // -----------------------------------------------------------------------------------------------------------------
    RESOURCE_NOT_FOUND(FAIL.code, "资源丢失"),
    RESOURCE_DUPLICATE(FAIL.code, "资源重复"),
    RESOURCE_INVALID(FAIL.code, "资源无效"),
    // -----------------------------------------------------------------------------------------------------------------
    SERVER_TIME(FAIL.code, "服务器时间异常"),
    SERVER_CODE(FAIL.code, "服务器代码异常"),
    // -----------------------------------------------------------------------------------------------------------------
    IM_PROTOCOL(FAIL.code, "通讯异常"),
    IM_CONNECT(FAIL.code, "连接异常"),
    IM_DECODE(FAIL.code, "解码异常"),
    IM_ENCODE(FAIL.code, "编码异常"),
    // -----------------------------------------------------------------------------------------------------------------
    LOGIN_TIMEOUT(FAIL.code, "登录超时或未登录"),
    LOGIN_AUTH_INCORRECT(FAIL.code, "用户名或密码错误"),
    // -----------------------------------------------------------------------------------------------------------------
    USER_CONFINE(FAIL.code, "已被限制使用"),
    USER_PERMISSION(FAIL.code, "未授权或授权到期"),
    // -----------------------------------------------------------------------------------------------------------------
    ;

    public final int code;
    public final String msg;

    public static final int OK_COMMAND_MIN = 1000;
    public static final int OK_COMMAND_MAX = 1999;

    XCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
