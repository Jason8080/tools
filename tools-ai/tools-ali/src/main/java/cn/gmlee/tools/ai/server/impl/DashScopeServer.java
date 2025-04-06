package cn.gmlee.tools.ai.server.impl;

import cn.gmlee.tools.ai.assist.MultiAssist;
import cn.gmlee.tools.ai.conf.AliAiProperties;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import com.alibaba.dashscope.aigc.multimodalconversation.*;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.embeddings.MultiModalEmbeddingItemAudio;
import com.alibaba.dashscope.embeddings.MultiModalEmbeddingItemImage;
import com.alibaba.dashscope.embeddings.MultiModalEmbeddingItemText;
import com.alibaba.dashscope.embeddings.MultiModalEmbeddingItemVideo;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 百联平台服务.
 */
@Slf4j
@Service
@SuppressWarnings("all")
@RequiredArgsConstructor
public class DashScopeServer {

    private final AliAiProperties aliAiProperties;

    private final MultiModalConversation ali = new MultiModalConversation();

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
                .content(Arrays.asList(Collections.singletonMap("text", user))
                ).build();
        MultiModalConversationParam param = getMultiModalConversationParam(sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
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
                .content(Arrays.asList(Collections.singletonMap("image", image), Collections.singletonMap("text", user))
                ).build();
        MultiModalConversationParam param = getMultiModalConversationParam(sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
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
    public Flowable<String> askImage(String sys, String user, byte... image) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        String content = MultiAssist.base64Image("png", image);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("image", content), Collections.singletonMap("text", user))
                ).build();
        MultiModalConversationParam param = getMultiModalConversationParam(sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertText);
    }

    /**
     * 询问 (图片).
     *
     * @param sys    系统角色
     * @param user   用户输入
     * @param images 图片内容
     * @return flowable 输出内容
     */
    public Flowable<String> askImages(String sys, String user, String... images) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        List<MultiModalMessageItemBase> contents = Arrays.stream(images).map(image -> MultiModalEmbeddingItemImage.builder().image(image).build()).collect(Collectors.toList());
        contents.add(MultiModalEmbeddingItemText.builder().text(user).build());
        MultiModalConversationMessage userMessage = MultiModalConversationMessage.builder()
                .role(Role.USER.getValue())
                .content(contents)
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertText);
    }

    /**
     * 询问 (音频).
     *
     * @param sys   系统角色
     * @param user  用户输入
     * @param audio 音频内容
     * @return flowable 输出内容
     */
    public Flowable<String> askAudio(String sys, String user, String audio) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("audio", audio), Collections.singletonMap("text", user))
                ).build();
        MultiModalConversationParam param = getMultiModalConversationParam(sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertText);
    }


    /**
     * 询问 (音频).
     *
     * @param sys   系统角色
     * @param user  用户输入
     * @param audio 音频内容
     * @return flowable 输出内容
     */
    public Flowable<String> askAudio(String sys, String user, byte... audio) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        String content = MultiAssist.base64Audio("mp3", audio);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("audio", content), Collections.singletonMap("text", user))
                ).build();
        MultiModalConversationParam param = getMultiModalConversationParam(sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertText);
    }

    /**
     * 询问 (音频).
     *
     * @param sys    系统角色
     * @param user   用户输入
     * @param audios 音频内容
     * @return flowable 输出内容
     */
    public Flowable<String> askAudios(String sys, String user, String... audios) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        List<MultiModalMessageItemBase> contents = Arrays.stream(audios).map(audio -> MultiModalEmbeddingItemAudio.builder().audio(audio).build()).collect(Collectors.toList());
        contents.add(MultiModalEmbeddingItemText.builder().text(user).build());
        MultiModalConversationMessage userMessage = MultiModalConversationMessage.builder()
                .role(Role.USER.getValue())
                .content(contents)
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertText);
    }

    /**
     * 询问 (视频).
     *
     * @param sys   系统角色
     * @param user  用户输入
     * @param video 视频内容
     * @return flowable 输出内容
     */
    public Flowable<String> askVideo(String sys, String user, String video) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("video", video), Collections.singletonMap("text", user))
                ).build();
        MultiModalConversationParam param = getMultiModalConversationParam(sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertText);
    }

    /**
     * 询问 (视频).
     *
     * @param sys   系统角色
     * @param user  用户输入
     * @param video 视频内容
     * @return flowable 输出内容
     */
    public Flowable<String> askVideo(String sys, String user, byte... video) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        String content = MultiAssist.base64Video("mp4", video);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("video", content), Collections.singletonMap("text", user))
                ).build();
        MultiModalConversationParam param = getMultiModalConversationParam(sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertText);
    }


    /**
     * 询问 (视频).
     *
     * @param sys    系统角色
     * @param user   用户输入
     * @param videos 视频内容
     * @return flowable 输出内容
     */
    public Flowable<String> askVideos(String sys, String user, String... videos) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        List<MultiModalMessageItemBase> contents = Arrays.stream(videos).map(video -> MultiModalEmbeddingItemVideo.builder().video(video).build()).collect(Collectors.toList());
        contents.add(MultiModalEmbeddingItemText.builder().text(user).build());
        MultiModalConversationMessage userMessage = MultiModalConversationMessage.builder()
                .role(Role.USER.getValue())
                .content(contents)
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertText);
    }

    private static MultiModalMessage getTextMultiModalMessage(Role role, String text) {
        MultiModalMessage sysMessage = MultiModalMessage.builder()
                .role(role.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", text)))
                .build();
        return sysMessage;
    }

    private MultiModalConversationParam getMultiModalConversationParam(Object sysMessage, Object userMessage, String... modalities) {
        return ((MultiModalConversationParam.MultiModalConversationParamBuilder) MultiModalConversationParam.builder().apiKey(aliAiProperties.getApiKey())
                .message(sysMessage)
                .message(userMessage)
                .modalities(Arrays.asList(modalities))
                .audio(AudioParameters.builder().voice(AudioParameters.Voice.CHERRY).build())
                .model(aliAiProperties.getAliModel()))
                .build();
    }

    private String convertText(MultiModalConversationResult result) {
//        ExceptionUtil.sandbox(() -> logger(result));
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
        StringBuffer sb = new StringBuffer("\r\n-------------------- {} --------------------");
        sb.append("\r\n消耗: {}/tokens; \t输入: {}/tokens; \t输出: {}/tokens");
        sb.append("\r\n图片: {}/tokens; \t音频: {}/tokens; \t视频: {}/tokens");
        sb.append("\r\n");
        log.info(sb.toString(), requestId,
                NullUtil.get(usage.getTotalTokens(), 0),
                NullUtil.get(usage.getInputTokens(), 0),
                NullUtil.get(usage.getOutputTokens(), 0),
                NullUtil.get(usage.getImageTokens(), 0),
                NullUtil.get(usage.getAudioTokens(), 0),
                NullUtil.get(usage.getVideoTokens(), 0)
        );
    }
}
