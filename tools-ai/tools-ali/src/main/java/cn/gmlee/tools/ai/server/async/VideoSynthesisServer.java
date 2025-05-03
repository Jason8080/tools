package cn.gmlee.tools.ai.server.async;

import cn.gmlee.tools.ai.conf.AliAiProperties;
import cn.gmlee.tools.base.util.ExceptionUtil;
import com.alibaba.dashscope.aigc.videosynthesis.*;
import com.alibaba.dashscope.task.AsyncTaskListParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 百炼平台服务.
 */
@Slf4j
@RequiredArgsConstructor
public class VideoSynthesisServer {

    private final AliAiProperties aliAiProperties;

    private final VideoSynthesis ali = new VideoSynthesis();

    /**
     * 询问.
     *
     * @param prompt the prompt
     * @return flowable 输出内容
     */
    public VideoSynthesisOutput ask(String prompt) {
        return ask(aliAiProperties.getDefaultModel(), prompt);
    }

    /**
     * 询问.
     *
     * @param model  the model
     * @param prompt the prompt
     * @return flowable 输出内容
     */
    public VideoSynthesisOutput ask(String model, String prompt) {
        return ask(model, prompt, aliAiProperties.getSpec(), aliAiProperties.getDuration());
    }

    /**
     * 询问.
     *
     * @param model    the model
     * @param prompt   the prompt
     * @param spec     the spec
     * @param duration the duration
     * @return flowable 输出内容
     */
    public VideoSynthesisOutput ask(String model, String prompt, String spec, Integer duration) {
        VideoSynthesisParam param = getVideoSynthesisParam(model, prompt, spec, duration);
        VideoSynthesisResult result = ExceptionUtil.suppress(() -> ali.asyncCall(param));
        return result.getOutput();
    }

    /**
     * 获取.
     *
     * @param taskId the task id
     * @return flowable 输出内容
     */
    public VideoSynthesisOutput get(String taskId) {
        VideoSynthesisResult result = ExceptionUtil.suppress(() -> ali.fetch(taskId, aliAiProperties.getApiKey()));
        return result.getOutput();
    }

    /**
     * 分页.
     *
     * @return flowable 输出内容
     */
    public VideoSynthesisListResult page() {
        AsyncTaskListParam param = AsyncTaskListParam.builder()
                .apiKey(aliAiProperties.getApiKey())
                .build();
        return ExceptionUtil.suppress(() -> ali.list(param));
    }


    private VideoSynthesisParam getVideoSynthesisParam(String model, String prompt, String spec, Integer duration) {
        return VideoSynthesisParam.builder()
                .apiKey(aliAiProperties.getApiKey())
                .model(model)
                .prompt(prompt)
                .seed(Integer.MAX_VALUE)
                .size(spec)
                .duration(duration)
                .build();
    }
}
