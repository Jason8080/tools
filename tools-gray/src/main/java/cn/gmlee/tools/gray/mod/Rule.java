package cn.gmlee.tools.gray.mod;

import lombok.Data;

import java.util.List;

/**
 * 规则定义.
 */
@Data
public class Rule {
    private Boolean enable;
    private List<String> content;
}
