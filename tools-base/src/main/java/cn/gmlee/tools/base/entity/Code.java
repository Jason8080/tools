package cn.gmlee.tools.base.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 通用编码实体.
 *
 * @author Jas°
 * @date 2021/11/10 (周三)
 */
@Getter
@Setter
@ToString
public class Code implements Serializable {
    /**
     * 编码
     */
    @NotEmpty(message = "编码是空")
    public String code;

    public Code() {
    }

    public Code(String code) {
        this.code = code;
    }
}
