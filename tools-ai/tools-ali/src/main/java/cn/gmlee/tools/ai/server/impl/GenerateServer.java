package cn.gmlee.tools.ai.server.impl;

import cn.gmlee.tools.ai.conf.AliAiProperties;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import com.alibaba.dashscope.aigc.completion.ChatCompletionChunk;
import com.alibaba.dashscope.aigc.completion.ChatCompletionUsage;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.GenerationUsage;
import com.alibaba.dashscope.aigc.multimodalconversation.AudioParameters;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 百联平台服务.
 */
@Slf4j
@SuppressWarnings("all")
@RequiredArgsConstructor
public class GenerateServer {

    private final AliAiProperties aliAiProperties;

    private final Generation ali = new Generation();

    /**
     * 询问.
     *
     * @param sys  系统角色
     * @param user 用户输入
     * @return flowable 输出内容
     */
    public Flowable<String> ask(String sys, String user) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", user)))
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(sysMessage, userMessage, "text");
        Flowable<GenerationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertText);
    }

    /**
     * 询问 (图片).
     *
     * @param sys   系统角色
     * @param user  用户输入
     * @param image 图片内容
     * @return flowable 输出内容
     */
    public Flowable<String> askImage(String sys, String user, String image) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("image", image), Collections.singletonMap("text", user)))
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(sysMessage, userMessage, "text");
        Flowable<GenerationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertText);
    }

    private static MultiModalMessage getTextMultiModalMessage(Role role, String text) {
        return MultiModalMessage.builder()
                .role(role.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", text)))
                .build();
    }

    private MultiModalConversationParam getMultiModalConversationParam(Object sysMessage, Object userMessage, String... modalities) {
        return ((MultiModalConversationParam.MultiModalConversationParamBuilder) MultiModalConversationParam.builder()
                .apiKey(aliAiProperties.getApiKey())
                .message(sysMessage)
                .message(userMessage)
                .enableSearch(aliAiProperties.getEnableSearch())
                .modalities(Arrays.asList(modalities))
                .audio(AudioParameters.builder().voice(AudioParameters.Voice.CHERRY).build())
                .model(aliAiProperties.getDefaultModel()))
                .build();
    }

    private String convertText(GenerationResult result) {
        ExceptionUtil.sandbox(() -> logger(result));
        GenerationOutput output = result.getOutput();
        List<GenerationOutput.Choice> choices = output.getChoices();
        if (BoolUtil.isEmpty(choices)) {
            return output.getFinishReason().equalsIgnoreCase("stop") ? output.getText() : "";
        }
        List<String> texts = choices.stream()
                .filter(Objects::nonNull)
                .map(GenerationOutput.Choice::getMessage)
                .filter(Objects::nonNull)
                .map(Message::getContent)
                .filter(BoolUtil::notEmpty)
                .collect(Collectors.toList());
        return texts.stream().collect(Collectors.joining());
    }

    private static void logger(GenerationResult result) {
        String requestId = result.getRequestId();
        GenerationUsage usage = result.getUsage();
        StringBuffer sb = new StringBuffer("\r\n-------------------- {} --------------------");
        sb.append("\r\n消耗: {}/tokens; \t输入: {}/tokens; \t输出: {}/tokens");
        sb.append("\r\n图片: {}/tokens; \t音频: {}/tokens; \t视频: {}/tokens");
        sb.append("\r\n");
        log.debug(sb.toString(), requestId,
                NullUtil.get(usage.getTotalTokens(), 0),
                NullUtil.get(usage.getInputTokens(), 0),
                NullUtil.get(usage.getOutputTokens(), 0)
        );
    }

    private static void logger(ChatCompletionChunk result) {
        ChatCompletionUsage usage = result.getUsage();

        StringBuffer sb = new StringBuffer("\r\n-------------------- {} --------------------");
        sb.append("\r\n消耗: {}/tokens; \t输入: {}/tokens; \t输出: {}/tokens");
        sb.append("\r\n图片: {}/tokens; \t音频: {}/tokens; \t视频: {}/tokens");
        sb.append("\r\n");
        log.debug(sb.toString(), "*",
                NullUtil.get(usage.getTotalTokens(), 0),
                NullUtil.get(usage.getPromptTokens(), 0),
                NullUtil.get(usage.getCompletionTokens(), 0)
        );
    }
}
