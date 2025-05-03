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

    public String getAudioFormat() {
        Mode mode = models.get(defaultModel);
        if (mode == null) {
            return new Mode().getAudioFormat();
        }
        return mode.getAudioFormat();
    }

    public String getSpec() {
        Mode mode = models.get(defaultModel);
        if (mode == null) {
            return new Mode().getSpec();
        }
        return mode.getSpec();
    }

    @Data
    public static class Mode {
        private Boolean enableSearch = Boolean.FALSE; // 是否开启搜索
        private Boolean hasThoughts = Boolean.FALSE; // 是否展示意图
        private String audioFormat = "pcm"; // 默认音频格式
        private String spec = "1024*1024"; // 默认音频格式
    }
}
