package cn.gmlee.tools.bit.conf;

import cn.gmlee.tools.base.mod.Mode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

/**
 * Bit Ai配置类.
 */
@Data
@ConfigurationProperties(prefix = "tools.ai.bit")
public class BitAiProperties {
    private String spaceId;
    private String agentKey;
    private String appId;
    private String apiKey;
    private String defaultModel = "doubao-seed-1-6-250615";
    private Map<String, Mode> models = Collections.singletonMap(defaultModel, new Mode());

    public Boolean getEnableSearch() {
        Mode mode = models.get(defaultModel);
        if (mode == null) {
            return new Mode().getEnableSearch();
        }
        return mode.getEnableSearch();
    }

    public Boolean getEnableThinking() {
        Mode mode = models.get(defaultModel);
        if (mode == null) {
            return new Mode().getEnableThinking();
        }
        return mode.getEnableThinking();
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

    public Integer getDuration() {
        Mode mode = models.get(defaultModel);
        if (mode == null) {
            return new Mode().getDuration();
        }
        return mode.getDuration();
    }

    public Integer getNum() {
        Mode mode = models.get(defaultModel);
        if (mode == null) {
            return new Mode().getNum();
        }
        return mode.getNum();
    }

    public Integer getSeed() {
        Mode mode = models.get(defaultModel);
        if (mode == null) {
            return new Mode().getSeed();
        }
        return mode.getSeed();
    }
}
