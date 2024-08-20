package cn.gmlee.tools.sign.fiegn;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.sign.anno.Sign;
import cn.gmlee.tools.sign.assist.SignAssist;
import cn.gmlee.tools.sign.conf.LdwSignProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public class SignRequestInterceptor<T> implements RequestInterceptor {

    private final LdwSignProperties ldwSignProperties;

    @Override
    public void apply(RequestTemplate template) {
        // 未知方法: 不签
        Method method = template.methodMetadata().method();
        if (method == null) {
            return;
        }
        // 没有标记: 不签
        Sign sign = method.getAnnotation(Sign.class);
        if (sign == null) {
            return;
        }
        // 没有配置: 不签
        String appKey = ldwSignProperties.getApp().get(sign.appId());
        if (BoolUtil.isEmpty(appKey)) {
            return;
        }
        SignAssist.set(sign);
    }
}
