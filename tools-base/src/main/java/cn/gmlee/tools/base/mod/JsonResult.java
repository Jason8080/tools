package cn.gmlee.tools.base.mod;

import cn.gmlee.tools.base.enums.XCode;
import lombok.Data;

import java.io.Serializable;

/**
 * Json响应对象
 * <p>
 * Http状态码
 *
 * @param <T> 响应对象泛型
 * @author Jas°
 */
@Data
public class JsonResult<T> implements Serializable {
    /**
     * 操作失败.
     */
    public static final JsonResult FAIL = new JsonResult(XCode.UNKNOWN5000.code, "操作失败");
    /**
     * 操作成功.
     */
    public static final JsonResult OK = new JsonResult(XCode.OK1000.code, "操作成功");


    /**
     * 原则上, 所有字段不可更改
     */
    private Integer code;
    private String msg;
    private T data;
    private Long responseTime = System.currentTimeMillis();

    public JsonResult() {
        this.code = OK.code;
        this.msg = OK.msg;
        this.data = null;
    }

    public JsonResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public JsonResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = null;
    }

    public JsonResult(XCode xCode) {
        this(xCode.code, xCode.msg);
    }

    public JsonResult(XCode xCode, T data) {
        this(xCode.code, xCode.msg, data);
    }

    public JsonResult newly(String msg) {
        return new JsonResult(this.code, msg, this.data);
    }

    public JsonResult newly(T data) {
        return new JsonResult(this.code, this.msg, data);
    }

    public JsonResult newly(Integer code, String msg) {
        return new JsonResult(code, msg, data);
    }

    public JsonResult newly(T data, String msg) {
        return new JsonResult(code, msg, data);
    }
}
