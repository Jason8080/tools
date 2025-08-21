package cn.gmlee.tools.bit.server.stream;

import cn.gmlee.tools.base.mod.Ask;
import cn.gmlee.tools.base.util.*;
import cn.gmlee.tools.bit.assist.AskAssist;
import cn.gmlee.tools.bit.conf.BitAiProperties;
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
     * @param sys  系统角色
     * @param user 用户输入
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
        // 添加系统提示词
        addSystemMessage(sys, messages);
        // 添加用户提示词
        addUserMessage(user, messages);
        // 构建聊天请求体
        ChatCompletionRequest request = getChatCompletionRequest(model, messages);
        Flowable<ChatCompletionChunk> flowable = bit.streamChatCompletion(request);
        return flowable.map(this::convertAsk).filter(Ask::notEmpty);
    }

    /**
     * 询问 (图片).
     *
     * @param sys   系统角色
     * @param user  用户输入
     * @param image 图片内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askImage(String sys, String user, String image) {
        return askImage(bitAiProperties.getDefaultModel(), sys, user, image);
    }


    /**
     * 询问 (图片).
     *
     * @param model 模型名称
     * @param sys   系统角色
     * @param user  用户输入
     * @param image 图片内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askImage(String model, String sys, String user, String image) {
        return askImages(model, sys, user, image);
    }

    /**
     * 询问 (图片).
     *
     * @param sys   系统角色
     * @param user  用户输入
     * @param image 图片内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askImage(String sys, String user, byte... image) {
        return askImage(bitAiProperties.getDefaultModel(), sys, user, image);
    }

    /**
     * 询问 (图片).
     *
     * @param model 模型名称
     * @param sys   系统角色
     * @param user  用户输入
     * @param image 图片内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askImage(String model, String sys, String user, byte... image) {
        List<ChatMessage> messages = new ArrayList<>();
        // 添加系统提示词
        addSystemMessage(sys, messages);
        // 添加用户提示词
        addUserMessage(getChatCompletionContentParts(user, image), messages);
        // 构建聊天请求体
        ChatCompletionRequest request = getChatCompletionRequest(model, messages);
        Flowable<ChatCompletionChunk> flowable = bit.streamChatCompletion(request);
        return flowable.map(this::convertAsk).filter(Ask::notEmpty);
    }

    /**
     * 询问 (图片).
     *
     * @param model  模型名称
     * @param sys    系统角色
     * @param user   用户输入
     * @param images 图片内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askImages(String model, String sys, String user, String... images) {
        List<ChatMessage> messages = new ArrayList<>();
        // 添加系统提示词
        addSystemMessage(sys, messages);
        // 添加用户提示词
        addUserMessage(getChatCompletionContentParts(user, images), messages);
        // 构建聊天请求体
        ChatCompletionRequest request = getChatCompletionRequest(model, messages);
        Flowable<ChatCompletionChunk> flowable = bit.streamChatCompletion(request);
        return flowable.map(this::convertAsk).filter(Ask::notEmpty);
    }

    private ChatCompletionRequest getChatCompletionRequest(String model, List<ChatMessage> messages) {
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(CharUtil.firstNonEmpty(model, bitAiProperties.getDefaultModel()))
                .messages(messages)
                .thinking(getThinking())
                .build();
        return chatCompletionRequest;
    }

    private ChatCompletionRequest.ChatCompletionRequestThinking getThinking() {
        String type = "auto";
        if (bitAiProperties.getEnableThinking() != null) {
            type = bitAiProperties.getEnableThinking() ? "enabled" : "disabled";
        }
        return new ChatCompletionRequest.ChatCompletionRequestThinking(type);
    }

    private static void addSystemMessage(String sys, List<ChatMessage> messages) {
        ChatMessage systemMessage = BoolUtil.isEmpty(sys) ? null : ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM)
                .content(sys).build();
        QuickUtil.notNull(systemMessage, x -> messages.add(x));
    }

    private static void addUserMessage(String user, List<ChatMessage> messages) {
        ChatMessage systemMessage = BoolUtil.isEmpty(user) ? null : ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(user).build();
        QuickUtil.notNull(systemMessage, x -> messages.add(x));
    }

    private void addUserMessage(List<ChatCompletionContentPart> multiParts, List<ChatMessage> messages) {
        AssertUtil.notEmpty(multiParts, "用户输入不能为空");
        ChatMessage userMessage = BoolUtil.isEmpty(multiParts) ? null : ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .multiContent(multiParts)
                .build();
        QuickUtil.notNull(userMessage, x -> messages.add(x));
    }

    private static List<ChatCompletionContentPart> getChatCompletionContentParts(String user, String... images) {
        List<ChatCompletionContentPart> multiParts = new ArrayList<>();
        for (String image : images) {
            if(BoolUtil.isEmpty(image)){
                continue;
            }
            ChatCompletionContentPart part = ChatCompletionContentPart.builder().type("image_url")
                    .imageUrl(new ChatCompletionContentPart.ChatCompletionContentPartImageURL(image))
                    .build();
            multiParts.add(part);
        }
        multiParts.add(ChatCompletionContentPart.builder().type("text").text(user).build());
        return multiParts;
    }

    private static List<ChatCompletionContentPart> getChatCompletionContentParts(String user, byte... image) {
        return getChatCompletionContentParts(user, Base64Util.encode(null, image));
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
