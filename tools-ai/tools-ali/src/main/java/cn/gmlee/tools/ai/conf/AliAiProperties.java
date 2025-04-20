package cn.gmlee.tools.ai.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

/**
 * Ali Ai配置类.
 *
 * tools.ai.ali.defaultModel=qwen-omni-turbo-latest
 * tools.ai.ali.defaultModel=qwen-vl-max-latest
 * tools.ai.ali.defaultModel=llama-4-scout-17b-16e-instruct
 * tools.ai.ali.defaultModel=qvq-max-latest
 * tools.ai.ali.defaultModel=qwen-vl-ocr-latest
 * tools.ai.ali.defaultModel=qwen2.5-omni-7b
 * tools.ai.ali.defaultModel=qvq-72b-preview
 * tools.ai.ali.defaultModel=qwen2.5-vl-72b-instruct
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

    /**
     * Gets enable search.
     *
     * @return the enable search
     */
    public Boolean getEnableSearch() {
        Mode mode = models.get(defaultModel);
        if (mode == null) {
            return new Mode().getEnableSearch();
        }
        return mode.getEnableSearch();
    }

    /**
     * The type Mode.
     */
    @Data
    public static class Mode {
        private Boolean enableSearch = Boolean.FALSE;
    }
}
