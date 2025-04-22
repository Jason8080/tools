package cn.gmlee.tools.ai.server.impl;

import cn.gmlee.tools.ai.conf.AliAiProperties;
import cn.gmlee.tools.base.builder.KvBuilder;
import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationOutput;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.utils.JsonUtils;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 百联平台服务.
 */
@Slf4j
@SuppressWarnings("all")
@RequiredArgsConstructor
public class ApplicationServer {

    private final AliAiProperties aliAiProperties;

    private final Application ali = new Application();

    /**
     * 询问.
     *
     * @param prompt 提示词
     * @param kvs    参数集
     * @return flowable 输出内容
     */
    public Flowable<String> ask(String prompt, Kv... kvs) {
        ApplicationParam param = getApplicationParamParam(prompt, kvs);
        Flowable<ApplicationResult> flowable = ExceptionUtil.suppress(() -> ali.streamCall(param));
        return flowable.map(this::convertText);
    }

    private ApplicationParam getApplicationParamParam(String prompt, Kv... kvs) {
        return ApplicationParam.builder()
                .appId(aliAiProperties.getAppId())
                .apiKey(aliAiProperties.getApiKey())
                .hasThoughts(aliAiProperties.getHasThoughts())
                .bizParams(JsonUtils.toJsonObject(KvBuilder.map(kvs)))
                .incrementalOutput(false)
                .prompt(prompt)
                .seed(0)
                .build();
    }

    private String convertText(ApplicationResult result) {
        ApplicationOutput output = result.getOutput();
        return NullUtil.get(output.getText());
    }
}
