package cn.gmlee.tools.ai.server.impl;

import cn.gmlee.tools.ai.conf.AliAiProperties;
import cn.gmlee.tools.base.mod.Kv;
import com.alibaba.dashscope.audio.asr.translation.TranslationRecognizerRealtime;
import com.alibaba.dashscope.audio.asr.translation.results.TranslationRecognizerResult;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 百炼平台服务.
 */
@Slf4j
@RequiredArgsConstructor
public class TranslationServer {

    private final AliAiProperties aliAiProperties;

    private final TranslationRecognizerRealtime ali = new TranslationRecognizerRealtime();

    /**
     * 询问.
     *
     * @param sys  系统角色
     * @param user 用户输入
     * @return flowable 输出内容 key原文、val译文
     */
    public Flowable<Kv<String, String>> ask(String sys, String user) {
        return null;
    }
}
