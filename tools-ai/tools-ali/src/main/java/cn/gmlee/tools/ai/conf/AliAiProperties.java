package cn.gmlee.tools.ai.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

/**
 * The type Ali ai properties.
 */
@Data
@ConfigurationProperties(prefix = "tools.ai.ali")
public class AliAiProperties {
    private String spaceId;
    private String agentKey;
    private String appId;
    private String apiKey;
    private String defaultModel = "qwen-omni-turbo";
    private Map<String, Mode> models = Collections.singletonMap(defaultModel, new Mode());

    public Boolean getEnableSearch() {
        Mode mode = models.get(defaultModel);
        if (mode == null) {
            return new Mode().getEnableSearch();
        }
        return mode.getEnableSearch();
    }

    public Boolean getHasThoughts() {
        Mode mode = models.get(defaultModel);
        if (mode == null) {
            return new Mode().getHasThoughts();
        }
        return mode.getHasThoughts();
    }

    /**
     * The type Mode.
     */
    @Data
    public static class Mode {
        private Boolean enableSearch = Boolean.FALSE; // 是否开启搜索
        private Boolean hasThoughts = Boolean.FALSE; // 是否展示意图
    }
}
