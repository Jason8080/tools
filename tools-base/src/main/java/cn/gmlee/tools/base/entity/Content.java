package cn.gmlee.tools.base.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 通用内容实体.
 *
 * @author Jas°
 * @date 2021/11/10 (周三)
 */
@Getter
@Setter
@ToString
public class Content extends Id implements Serializable {
    /**
     * 内容
     */
    @NotEmpty(message = "内容是空")
    public String content;

    public Content() {
    }

    public Content(String content) {
        this.content = content;
    }
}
