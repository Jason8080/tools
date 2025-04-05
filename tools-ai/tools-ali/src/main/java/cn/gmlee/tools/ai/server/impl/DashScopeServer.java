package cn.gmlee.tools.ai.server.impl;

import cn.gmlee.tools.ai.conf.AliAiProperties;
import cn.gmlee.tools.base.util.ExceptionUtil;
import com.alibaba.dashscope.aigc.multimodalconversation.AudioParameters;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;

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
    public void ask(String sys, String user) {
        MultiModalMessage sysMessage = MultiModalMessage.builder()
                .role(Role.SYSTEM.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", sys)))
                .build();
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", user))
                ).build();
        MultiModalConversationParam param = ((MultiModalConversationParam.MultiModalConversationParamBuilder)MultiModalConversationParam
                .builder().apiKey(aliAiProperties.getApiKey())
                .message(sysMessage)
                .message(userMessage)
                .modalities(Arrays.asList("text"))
                .audio(AudioParameters.builder().voice(AudioParameters.Voice.CHERRY).build())
                .model(aliAiProperties.getAliModel()))
                .build();

        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        flowable.blockingForEach((data) -> {
            System.out.printf("output=%s\n", data.getOutput());
            System.out.printf("usage=%s\n\n", data.getUsage());
        });
    }
}
