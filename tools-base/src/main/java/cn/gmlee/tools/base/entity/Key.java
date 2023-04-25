package cn.gmlee.tools.base.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 通用唯一键实体.
 *
 * @author Jas°
 * @date 2021/11/10 (周三)
 */
@Getter
@Setter
@ToString
public class Key implements Serializable {
    /**
     * 唯一标识
     */
    @NotEmpty(message = "唯一标识是空")
    public String uniqueKey;

    public Key() {
    }

    public Key(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }
}
