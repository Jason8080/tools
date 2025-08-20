package cn.gmlee.tools.ai.assist;

import cn.gmlee.tools.base.mod.Ask;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.NullUtil;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type Ask assist.
 */
public class AskAssist {
    /**
     * Instantiates a new Ask.
     *
     * @param mmm the mmm
     * @return the ask
     */
    public static Ask create(MultiModalMessage mmm) {
        Ask ask = new Ask();
        ask.setThink(NullUtil.get(mmm.getReasoningContent()));
        List<Map<String, Object>> contents = mmm.getContent();
        if (BoolUtil.isEmpty(contents)) {
            return ask;
        }
        ask.setReply(contents.stream()
                .map(entry -> entry.get("text"))
                .filter(o -> o instanceof String)
                .map(String::valueOf)
                .collect(Collectors.joining()));
        return ask;
    }

    /**
     * Instantiates a new Ask.
     *
     * @param message the message
     * @return the ask
     */
    public static Ask create(Message message) {
        Ask ask = new Ask();
        ask.setThink(NullUtil.get(message.getReasoningContent()));
        ask.setReply(NullUtil.get(message.getContent()));
        return ask;
    }
}
