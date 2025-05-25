package cn.gmlee.tools.ai.mod;

import cn.gmlee.tools.base.util.BoolUtil;
import com.alibaba.dashscope.common.MultiModalMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class Ask implements Serializable {
    /**
     * 系统提示词
     */
    private String systemPrompt;
    /**
     * 用户提示词
     */
    private String userPrompt;
    /**
     * 多媒体文件
     */
    private List<String> files = new ArrayList<>();
    /**
     * 会话编号
     */
    private String sessionId;
    /**
     * 思考内容
     */
    private String think;

    /**
     * 回复内容
     */
    private String reply;

    public Ask() {
    }

    public Ask(MultiModalMessage mmm) {
        this.think = mmm.getReasoningContent();
        List<Map<String, Object>> contents = mmm.getContent();
        if (BoolUtil.isEmpty(contents)) {
            this.reply = "";
        }
        this.reply = contents.stream()
                .map(entry -> entry.get("text"))
                .filter(o -> o instanceof String)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    public Ask(List<Ask> asks) {
        if (BoolUtil.isEmpty(asks)) {
            return;
        }
        for (Ask ask : asks) {
            if (BoolUtil.isEmpty(this.systemPrompt)) {
                this.systemPrompt = ask.systemPrompt;
            }
            if (BoolUtil.isEmpty(this.userPrompt)) {
                this.userPrompt = ask.userPrompt;
            }
            if (BoolUtil.isEmpty(this.sessionId)) {
                this.sessionId = ask.sessionId;
            }
            this.think += ask.think;
            this.reply += ask.reply;
            this.files.addAll(ask.files);
        }
    }
}
