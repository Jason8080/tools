package cn.gmlee.tools.ai.server.stream;

import cn.gmlee.tools.ai.assist.AskAssist;
import cn.gmlee.tools.ai.conf.AliAiProperties;
import cn.gmlee.tools.base.mod.Ask;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import com.alibaba.dashscope.aigc.generation.*;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 百炼平台服务.
 */
@Slf4j
@RequiredArgsConstructor
public class GenerateServer {

    private final AliAiProperties aliAiProperties;

    private final Generation ali = new Generation();

    /**
     * 询问 (思考要求).
     *
     * @param sys   系统角色
     * @param user  用户输入
     * @return flowable 输出内容
     */
    public Flowable<Ask> ask(String sys, String user) {
        return ask(aliAiProperties.getDefaultModel(), sys, user);
    }

    /**
     * 询问 (思考要求).
     *
     * @param model 模型名称
     * @param sys   系统角色
     * @param user  用户输入
     * @return flowable 输出内容
     */
    public Flowable<Ask> ask(String model, String sys, String user) {
        Message sysMessage = getTextMessage(Role.SYSTEM, sys);
        Message userMessage = Message.builder()
                .role(Role.USER.getValue())
                .content(user)
                .build();
        GenerationParam param = getGenerationParam(model, sysMessage, userMessage);
        Flowable<GenerationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertAsk).filter(Ask::notEmpty);
    }

    private static Message getTextMessage(Role role, String text) {
        if (BoolUtil.isEmpty(text)) {
            return null;
        }
        return Message.builder()
                .role(role.getValue())
                .content(text)
                .build();
    }

    private GenerationParam getGenerationParam(String model, Message sysMessage, Message userMessage) {
        List<Message> messages = Arrays.asList(sysMessage, userMessage).stream().filter(Objects::nonNull).collect(Collectors.toList());
        return GenerationParam.builder()
                .apiKey(aliAiProperties.getApiKey())
                .model(model)
                .enableSearch(aliAiProperties.getEnableSearch())
                .enableThinking(aliAiProperties.getEnableThinking())
                .incrementalOutput(true)
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .seed(0)
                .build();
    }

    private Ask convertAsk(GenerationResult result) {
//        ExceptionUtil.sandbox(() -> logger(result));
        GenerationOutput output = result.getOutput();
        List<GenerationOutput.Choice> choices = output.getChoices();
        if (BoolUtil.isEmpty(choices)) {
            return new Ask(output.getText());
        }
        List<Ask> asks = choices.stream()
                .filter(Objects::nonNull)
                .map(GenerationOutput.Choice::getMessage)
                .filter(Objects::nonNull)
                .map(AskAssist::create)
                .filter(Ask::notEmpty)
                .collect(Collectors.toList());
        return new Ask(asks);
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
}
