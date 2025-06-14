package cn.gmlee.tools.ai.mod;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.NullUtil;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The type Ask.
 */
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

    /**
     * Instantiates a new Ask.
     */
    public Ask() {
        this.think = "";
        this.reply = "";
    }

    /**
     * Instantiates a new Ask.
     *
     * @param reply the reply
     */
    public Ask(String reply) {
        this();
        this.reply = NullUtil.get(reply);
    }

    /**
     * Instantiates a new Ask.
     *
     * @param mmm the mmm
     */
    public Ask(MultiModalMessage mmm) {
        this();
        this.think = NullUtil.get(mmm.getReasoningContent());
        List<Map<String, Object>> contents = mmm.getContent();
        if (BoolUtil.isEmpty(contents)) {
            return;
        }
        this.reply = contents.stream()
                .map(entry -> entry.get("text"))
                .filter(o -> o instanceof String)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    /**
     * Instantiates a new Ask.
     *
     * @param message the message
     */
    public Ask(Message message) {
        this();
        this.think = NullUtil.get(message.getReasoningContent());
        this.reply = NullUtil.get(message.getContent());
    }

    /**
     * Instantiates a new Ask.
     *
     * @param asks the asks
     */
    public Ask(List<Ask> asks) {
        this();
        if (BoolUtil.isEmpty(asks)) {
            return;
        }
        this.think = asks.stream().filter(Objects::nonNull).filter(Ask::notEmpty).map(Ask::getThink).collect(Collectors.joining());
        this.reply = asks.stream().filter(Objects::nonNull).filter(Ask::notEmpty).map(Ask::getReply).collect(Collectors.joining());
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
            if (BoolUtil.isEmpty(this.files)) {
                this.files = ask.files;
            }
        }
    }

    /**
     * Is empty boolean.
     *
     * @return the boolean
     */
    public boolean isEmpty(){
        return BoolUtil.allEmpty(this.think, this.reply);
    }

    /**
     * Not empty boolean.
     *
     * @return the boolean
     */
    public boolean notEmpty(){
        return !isEmpty();
    }
}
