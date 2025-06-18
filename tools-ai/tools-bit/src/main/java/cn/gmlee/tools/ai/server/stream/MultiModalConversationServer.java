package cn.gmlee.tools.ai.server.stream;

import cn.gmlee.tools.ai.assist.MultiAssist;
import cn.gmlee.tools.ai.conf.AliAiProperties;
import cn.gmlee.tools.ai.conf.BitAiProperties;
import cn.gmlee.tools.ai.mod.Ask;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.embeddings.MultiModalEmbeddingItemAudio;
import com.alibaba.dashscope.embeddings.MultiModalEmbeddingItemImage;
import com.alibaba.dashscope.embeddings.MultiModalEmbeddingItemText;
import com.alibaba.dashscope.embeddings.MultiModalEmbeddingItemVideo;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChunk;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import com.volcengine.ark.runtime.utils.SSE;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 方舟平台服务.
 */
@Slf4j
@SuppressWarnings("all")
@RequiredArgsConstructor
public class MultiModalConversationServer {

    private final BitAiProperties bitAiProperties;

    private final ArkService bit;

    /**
     * 询问.
     *
     * @param model 模型名称
     * @param sys   系统角色
     * @param user  用户输入
     * @return flowable 输出内容
     */
    public Flowable<ChatCompletionChunk> ask(String sys, String user) {
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
    public Flowable<ChatCompletionChunk> ask(String model, String sys, String user) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM)
                .content(sys).build();
        ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(user).build();
        messages.add(systemMessage);
        messages.add(userMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .build();
        return bit.streamChatCompletion(chatCompletionRequest);
    }
}
