package cn.gmlee.tools.gray.mod;

import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 应用定义.
 */
@Data
public class App {
    private Boolean enable = true;
    private List<String> versions = Collections.emptyList();
    private Map<String, Rule> rules = Collections.emptyMap();
}
