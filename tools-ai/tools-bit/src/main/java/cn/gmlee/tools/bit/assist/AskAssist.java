package cn.gmlee.tools.bit.assist;

import cn.gmlee.tools.base.mod.Ask;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.NullUtil;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;

/**
 * The type Ask assist.
 */
public class AskAssist {

    /**
     * Create ask.
     *
     * @param cm the cm
     * @return the ask
     */
    public static Ask create(ChatMessage cm) {
        Ask ask = new Ask();
        ask.setThink(NullUtil.get(cm.getReasoningContent()));
        Object contents = cm.getContent();
        if(contents instanceof String){
            ask.setReply(contents.toString());
        }
        return ask;
    }
}
