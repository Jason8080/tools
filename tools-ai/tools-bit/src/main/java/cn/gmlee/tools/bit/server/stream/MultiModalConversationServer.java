package cn.gmlee.tools.bit.server.stream;

import cn.gmlee.tools.base.mod.Ask;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.bit.assist.AskAssist;
import cn.gmlee.tools.bit.conf.BitAiProperties;
import com.volcengine.ark.runtime.model.Usage;
import com.volcengine.ark.runtime.model.completion.chat.*;
import com.volcengine.ark.runtime.service.ArkService;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 方舟平台服务.
 */
@Slf4j
@SuppressWarnings("all")
@RequiredArgsConstructor
public class MultiModalConversationServer {

    private final ArkService bit;

    private final BitAiProperties bitAiProperties;

    /**
     * 询问.
     *
     * @param model 模型名称
     * @param sys   系统角色
     * @param user  用户输入
     * @return flowable 输出内容
     */
    public Flowable<Ask> ask(String sys, String user) {
        return ask(bitAiProperties.getDefaultModel(), sys, user);
    }

    /**
     * 询问.
     *
     * @param model 模型名称
     * @param sys   系统角色
     * @param user  用户输入
     * @return flowable 输出内容
     */
    public Flowable<Ask> ask(String model, String sys, String user) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = BoolUtil.isEmpty(sys) ? null : ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM)
                .content(sys).build();
        ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(user).build();
        QuickUtil.notNull(systemMessage, x -> messages.add(x));
        messages.add(userMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .build();
        Flowable<ChatCompletionChunk> flowable = bit.streamChatCompletion(chatCompletionRequest);
        return flowable.map(this::convertAsk).filter(Ask::notEmpty);
    }

    private Ask convertAsk(ChatCompletionChunk chunk) {
        List<ChatCompletionChoice> choices = chunk.getChoices();
        if (BoolUtil.isEmpty(choices)) {
            return new Ask();
        }
        List<Ask> asks = choices.stream()
                .filter(Objects::nonNull)
                .map(ChatCompletionChoice::getMessage)
                .filter(Objects::nonNull)
                .map(AskAssist::create)
                .filter(Ask::notEmpty)
                .collect(Collectors.toList());
        return new Ask(asks);
    }
}
