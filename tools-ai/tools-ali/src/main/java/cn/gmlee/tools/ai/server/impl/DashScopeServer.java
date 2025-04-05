package cn.gmlee.tools.ai.server.impl;

import cn.gmlee.tools.ai.conf.AliAiProperties;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import com.alibaba.dashscope.aigc.multimodalconversation.*;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 百联平台服务.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashScopeServer {

    private final AliAiProperties aliAiProperties;

    private final MultiModalConversation ali = new MultiModalConversation();

    /**
     * 询问.
     *
     * @param sys  the sys msg
     * @param user the u msg
     * @return the generation result
     */
    public Flowable<String> ask(String sys, String user) {
        MultiModalMessage sysMessage = MultiModalMessage.builder()
                .role(Role.SYSTEM.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", sys)))
                .build();
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", user))
                ).build();
        MultiModalConversationParam param = ((MultiModalConversationParam.MultiModalConversationParamBuilder) MultiModalConversationParam
                .builder().apiKey(aliAiProperties.getApiKey())
                .message(sysMessage)
                .message(userMessage)
                .modalities(Arrays.asList("text"))
                .audio(AudioParameters.builder().voice(AudioParameters.Voice.CHERRY).build())
                .model(aliAiProperties.getAliModel()))
                .build();
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertText);
    }


    public String convertText(MultiModalConversationResult result) {
        ExceptionUtil.sandbox(() -> logger(result));
        MultiModalConversationOutput output = result.getOutput();
        List<MultiModalConversationOutput.Choice> choices = output.getChoices();
        if (BoolUtil.isEmpty(choices)) {
            return "";
        }
        List<String> texts = choices.stream()
                .filter(Objects::nonNull)
                .map(MultiModalConversationOutput.Choice::getMessage)
                .filter(Objects::nonNull)
                .map(MultiModalMessage::getContent)
                .filter(BoolUtil::notEmpty)
                .map(x -> x.stream()
                        .map(entry -> entry.get("text"))
                        .filter(o -> o instanceof String)
                        .map(String::valueOf)
                        .collect(Collectors.joining()))
                .collect(Collectors.toList());
        return texts.stream().collect(Collectors.joining());
    }

    private static void logger(MultiModalConversationResult result) {
        String requestId = result.getRequestId();
        MultiModalConversationUsage usage = result.getUsage();
        log.info("\r\n-------------------- {} --------------------\r\n", requestId);
        log.info("\r\n消耗: {}/tokens; 输入: {}/tokens; 输出: {}/tokens\r\n",
                NullUtil.get(usage.getTotalTokens(), 0),
                NullUtil.get(usage.getInputTokens(), 0),
                NullUtil.get(usage.getOutputTokens(), 0));
        log.info("\r\n图片: {}/tokens; 音频: {}/tokens; 视频: {}/tokens\r\n",
                requestId, usage.getTotalTokens(), usage.getInputTokens(), usage.getOutputTokens(),
                NullUtil.get(usage.getImageTokens(), 0),
                NullUtil.get(usage.getAudioTokens(), 0),
                NullUtil.get(usage.getVideoTokens(), 0)
        );
    }
}
