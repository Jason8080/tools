package cn.gmlee.tools.ai.server.stream;

import cn.gmlee.tools.ai.assist.MultiAssist;
import cn.gmlee.tools.ai.conf.AliAiProperties;
import cn.gmlee.tools.ai.mod.Ask;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 百炼平台服务.
 */
@Slf4j
@SuppressWarnings("all")
@RequiredArgsConstructor
public class MultiModalConversationServer {

    private final AliAiProperties aliAiProperties;

    private final MultiModalConversation ali = new MultiModalConversation();

    /**
     * 询问.
     *
     * @param sys  系统角色
     * @param user 用户输入
     * @return flowable 输出内容
     */
    public Flowable<Ask> ask(String sys, String user) {
        return ask(aliAiProperties.getDefaultModel(), sys, user);
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
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", user)))
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(model, sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
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
        return askImage(aliAiProperties.getDefaultModel(), sys, user, image);
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
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("image", image), Collections.singletonMap("text", user)))
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(model, sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
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
    public Flowable<Ask> askImage(String sys, String user, byte... image) {
        return askImage(aliAiProperties.getDefaultModel(), sys, user, image);
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
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        String content = MultiAssist.base64Image("png", image);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("image", content), Collections.singletonMap("text", user)))
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(model, sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
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
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        List<MultiModalMessageItemBase> contents = Arrays.stream(images).map(image -> MultiModalEmbeddingItemImage.builder().image(image).build()).collect(Collectors.toList());
        contents.add(MultiModalEmbeddingItemText.builder().text(user).build());
        MultiModalConversationMessage userMessage = MultiModalConversationMessage.builder()
                .role(Role.USER.getValue())
                .content(contents)
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(model, sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertAsk).filter(Ask::notEmpty);
    }

    /**
     * 询问 (音频).
     *
     * @param sys   系统角色
     * @param user  用户输入
     * @param audio 音频内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askAudio(String sys, String user, String audio) {
        return askAudio(aliAiProperties.getDefaultModel(), sys, user, audio);
    }

    /**
     * 询问 (音频).
     *
     * @param model 模型名称
     * @param sys   系统角色
     * @param user  用户输入
     * @param audio 音频内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askAudio(String model, String sys, String user, String audio) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("audio", audio), Collections.singletonMap("text", user)))
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(model, sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertAsk).filter(Ask::notEmpty);
    }


    /**
     * 询问 (音频).
     *
     * @param sys   系统角色
     * @param user  用户输入
     * @param audio 音频内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askAudio(String sys, String user, byte... audio) {
        return askAudio(aliAiProperties.getDefaultModel(), sys, user, audio);
    }

    /**
     * 询问 (音频).
     *
     * @param model 模型名称
     * @param sys   系统角色
     * @param user  用户输入
     * @param audio 音频内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askAudio(String model, String sys, String user, byte... audio) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        String content = MultiAssist.base64Audio("mp3", audio);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("audio", content), Collections.singletonMap("text", user)))
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(model, sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertAsk).filter(Ask::notEmpty);
    }

    /**
     * 询问 (音频).
     *
     * @param sys    系统角色
     * @param user   用户输入
     * @param audios 音频内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askAudios(String sys, String user, String... audios) {
        return askAudios(aliAiProperties.getDefaultModel(), sys, user, audios);
    }

    /**
     * 询问 (音频).
     *
     * @param model  模型名称
     * @param sys    系统角色
     * @param user   用户输入
     * @param audios 音频内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askAudios(String model, String sys, String user, String... audios) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        List<MultiModalMessageItemBase> contents = Arrays.stream(audios).map(audio -> MultiModalEmbeddingItemAudio.builder().audio(audio).build()).collect(Collectors.toList());
        contents.add(MultiModalEmbeddingItemText.builder().text(user).build());
        MultiModalConversationMessage userMessage = MultiModalConversationMessage.builder()
                .role(Role.USER.getValue())
                .content(contents)
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(model, sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertAsk).filter(Ask::notEmpty);
    }

    /**
     * 询问 (视频).
     *
     * @param sys   系统角色
     * @param user  用户输入
     * @param video 视频内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askVideo(String sys, String user, String video) {
        return askVideo(aliAiProperties.getDefaultModel(), sys, user, video);
    }

    /**
     * 询问 (视频).
     *
     * @param model 模型名称
     * @param sys   系统角色
     * @param user  用户输入
     * @param video 视频内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askVideo(String model, String sys, String user, String video) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("video", video), Collections.singletonMap("text", user)))
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(model, sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertAsk).filter(Ask::notEmpty);
    }

    /**
     * 询问 (视频).
     *
     * @param sys   系统角色
     * @param user  用户输入
     * @param video 视频内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askVideo(String sys, String user, byte... video) {
        return askVideo(aliAiProperties.getDefaultModel(), sys, user, video);
    }

    /**
     * 询问 (视频).
     *
     * @param model 模型名称
     * @param sys   系统角色
     * @param user  用户输入
     * @param video 视频内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askVideo(String model, String sys, String user, byte... video) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        String content = MultiAssist.base64Video("mp4", video);
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Collections.singletonMap("video", content), Collections.singletonMap("text", user)))
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(model, sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertAsk).filter(Ask::notEmpty);
    }


    /**
     * 询问 (视频).
     *
     * @param sys    系统角色
     * @param user   用户输入
     * @param videos 视频内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askVideos(String sys, String user, String... videos) {
        return askVideos(aliAiProperties.getDefaultModel(), sys, user, videos);
    }

    /**
     * 询问 (视频).
     *
     * @param model  模型名称
     * @param sys    系统角色
     * @param user   用户输入
     * @param videos 视频内容
     * @return flowable 输出内容
     */
    public Flowable<Ask> askVideos(String model, String sys, String user, String... videos) {
        MultiModalMessage sysMessage = getTextMultiModalMessage(Role.SYSTEM, sys);
        List<MultiModalMessageItemBase> contents = Arrays.stream(videos).map(video -> MultiModalEmbeddingItemVideo.builder().video(video).build()).collect(Collectors.toList());
        contents.add(MultiModalEmbeddingItemText.builder().text(user).build());
        MultiModalConversationMessage userMessage = MultiModalConversationMessage.builder()
                .role(Role.USER.getValue())
                .content(contents)
                .build();
        MultiModalConversationParam param = getMultiModalConversationParam(model, sysMessage, userMessage, "text");
        Flowable<MultiModalConversationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertAsk).filter(Ask::notEmpty);
    }

    private static MultiModalMessage getTextMultiModalMessage(Role role, String text) {
        if (BoolUtil.isEmpty(text)) {
            return null;
        }
        return MultiModalMessage.builder()
                .role(role.getValue())
                .content(Arrays.asList(Collections.singletonMap("text", text)))
                .build();
    }

    private MultiModalConversationParam getMultiModalConversationParam(String model, Object sysMessage, Object userMessage, String... modalities) {
        if (sysMessage == null) {
            return getMultiModalConversationParam(model, userMessage, modalities);
        }
        return ((MultiModalConversationParam.MultiModalConversationParamBuilder) MultiModalConversationParam.builder()
                .apiKey(aliAiProperties.getApiKey())
                .message(sysMessage)
                .message(userMessage)
                .enableSearch(aliAiProperties.getEnableSearch())
                .modalities(Arrays.asList(modalities))
                .audio(AudioParameters.builder().voice(AudioParameters.Voice.CHERRY).build())
                .incrementalOutput(true)
                .model(model))
                .build();
    }

    private MultiModalConversationParam getMultiModalConversationParam(String model, Object userMessage, String... modalities) {
        return ((MultiModalConversationParam.MultiModalConversationParamBuilder) MultiModalConversationParam.builder()
                .apiKey(aliAiProperties.getApiKey())
                .message(userMessage)
                .enableSearch(aliAiProperties.getEnableSearch())
                .modalities(Arrays.asList(modalities))
                .audio(AudioParameters.builder().voice(AudioParameters.Voice.CHERRY).build())
                .incrementalOutput(true)
                .model(model))
                .build();
    }

    private MultiModalConversationParam getMultiModalConversationParam(Object sysMessage, Object userMessage, String... modalities) {
        List<Object> messages = Arrays.asList(sysMessage, userMessage).stream().filter(Objects::nonNull).collect(Collectors.toList());
        return ((MultiModalConversationParam.MultiModalConversationParamBuilder) MultiModalConversationParam.builder()
                .apiKey(aliAiProperties.getApiKey())
                .model(aliAiProperties.getDefaultModel()))
                .enableSearch(aliAiProperties.getEnableSearch())
                .modalities(Arrays.asList(modalities))
                .voice(AudioParameters.Voice.CHERRY)
                .incrementalOutput(true)
                .messages(messages)
                .seed(0)
                .build();
    }

    private Ask convertAsk(MultiModalConversationResult result) {
//        ExceptionUtil.sandbox(() -> logger(result));
        MultiModalConversationOutput output = result.getOutput();
        List<MultiModalConversationOutput.Choice> choices = output.getChoices();
        if (BoolUtil.isEmpty(choices)) {
            return new Ask();
        }
        List<Ask> asks = choices.stream()
                .filter(Objects::nonNull)
                .map(MultiModalConversationOutput.Choice::getMessage)
                .filter(Objects::nonNull)
                .map(Ask::new)
                .filter(Ask::notEmpty)
                .collect(Collectors.toList());
        return new Ask(asks);
    }

    private static void logger(MultiModalConversationResult result) {
        String requestId = result.getRequestId();
        MultiModalConversationUsage usage = result.getUsage();
        StringBuffer sb = new StringBuffer("\r\n-------------------- {} --------------------");
        sb.append("\r\n消耗: {}/tokens; \t输入: {}/tokens; \t输出: {}/tokens");
        sb.append("\r\n图片: {}/tokens; \t音频: {}/tokens; \t视频: {}/tokens");
        sb.append("\r\n");
        log.debug(sb.toString(), requestId,
                NullUtil.get(usage.getTotalTokens(), 0),
                NullUtil.get(usage.getInputTokens(), 0),
                NullUtil.get(usage.getOutputTokens(), 0),
                NullUtil.get(usage.getImageTokens(), 0),
                NullUtil.get(usage.getAudioTokens(), 0),
                NullUtil.get(usage.getVideoTokens(), 0)
        );
    }
}
