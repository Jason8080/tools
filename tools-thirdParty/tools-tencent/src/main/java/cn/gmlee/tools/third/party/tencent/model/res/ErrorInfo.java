package cn.gmlee.tools.third.party.tencent.model.res;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用错误响应信息
 * <p>
 *     {"errcode":41030,"errmsg":"invalid page rid: 5f86bdd6-15ceb0dc-3ad307da"}
 * </p>
 * @author Jas°
 * @date 2020/10/14 (周三)
 */
@Data
public class ErrorInfo implements Serializable {
    private Integer errcode;
    private String errmsg;
}
