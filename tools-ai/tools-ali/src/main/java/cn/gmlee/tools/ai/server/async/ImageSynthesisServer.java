package cn.gmlee.tools.ai.server.async;

import cn.gmlee.tools.ai.conf.AliAiProperties;
import cn.gmlee.tools.base.util.ExceptionUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.*;
import com.alibaba.dashscope.task.AsyncTaskListParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 百炼平台服务.
 */
@Slf4j
@RequiredArgsConstructor
public class ImageSynthesisServer {

    private final AliAiProperties aliAiProperties;

    private final ImageSynthesis ali = new ImageSynthesis();

    /**
     * 询问.
     *
     * @param prompt the prompt
     * @return flowable 输出内容
     */
    public ImageSynthesisOutput ask(String prompt) {
        return ask(aliAiProperties.getDefaultModel(), prompt, aliAiProperties.getSpec());
    }

    /**
     * 询问.
     *
     * @param model  the model
     * @param prompt the prompt
     * @return flowable 输出内容
     */
    public ImageSynthesisOutput ask(String model, String prompt) {
        return ask(model, prompt, aliAiProperties.getSpec());
    }

    /**
     * 询问.
     *
     * @param model  the model
     * @param prompt the prompt
     * @param spec   the spec
     * @return flowable 输出内容
     */
    public ImageSynthesisOutput ask(String model, String prompt, String spec) {
        ImageSynthesisParam param = getImageSynthesisParam(model, prompt, spec);
        ImageSynthesisResult result = ExceptionUtil.suppress(() -> ali.asyncCall(param));
        return result.getOutput();
    }

    /**
     * 获取.
     *
     * @param taskId the task id
     * @return flowable 输出内容
     */
    public ImageSynthesisOutput get(String taskId) {
        ImageSynthesisResult result = ExceptionUtil.suppress(() -> ali.fetch(taskId, aliAiProperties.getApiKey()));
        return result.getOutput();
    }

    /**
     * 分页.
     *
     * @return flowable 输出内容
     */
    public ImageSynthesisListResult page() {
        AsyncTaskListParam param = AsyncTaskListParam.builder()
                .apiKey(aliAiProperties.getApiKey())
                .build();
        return ExceptionUtil.suppress(() -> ali.list(param));
    }


    private ImageSynthesisParam getImageSynthesisParam(String model, String prompt, String spec) {
        return ImageSynthesisParam.builder()
                .apiKey(aliAiProperties.getApiKey())
                .model(model)
                .prompt(prompt)
                .seed(aliAiProperties.getSeed())
                .n(aliAiProperties.getNum())
                .size(spec)
                .build();
    }
}
