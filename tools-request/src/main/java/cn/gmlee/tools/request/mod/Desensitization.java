package cn.gmlee.tools.request.mod;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Jas°
 * @date 2021/4/29 (周四)
 */
@Data
public class Desensitization implements Serializable {
    /**
     * 在规则中使用正则表达式
     */
    private Boolean use = true;
    /**
     * 规则: 正则表达式 或 属性名称
     */
    private String rule;
}
