package cn.gmlee.tools.base.mod;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 通用请求实体
 *
 * @author Jas°
 * @date 2020/9/8 (周二)
 */
@Getter
@Setter
@ToString
public class PageRequest implements Serializable {
    public Integer current = 1;
    public Integer size = 5;

    public PageRequest() {
    }

    public PageRequest(Integer current, Integer size) {
        this.current = current;
        this.size = size;
    }

    public static PageRequest of() {
        return new PageRequest();
    }

    public static PageRequest of(Integer current, Integer size) {
        return new PageRequest(current, size);
    }
}
