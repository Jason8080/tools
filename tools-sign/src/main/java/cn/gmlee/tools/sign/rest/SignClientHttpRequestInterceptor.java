package cn.gmlee.tools.sign.rest;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.base.util.SignUtil;
import cn.gmlee.tools.sign.anno.Sign;
import cn.gmlee.tools.sign.assist.SignAssist;
import cn.gmlee.tools.sign.conf.LdwSignProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class SignClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final LdwSignProperties ldwSignProperties;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        try {

            // 请求签名
            signatureRequest(request, body);

            // 执行请求
            ClientHttpResponse response = execution.execute(request, body);

            // 验证响应
            validateResponse(response);

            return response;

        } finally {

            // 清理数据
            SignAssist.remove();

        }
    }

    private void signatureRequest(HttpRequest request, byte[] body) {
        Sign sign = SignAssist.get();
        String signature = SignUtil.signDirect(ldwSignProperties.getApp().get(sign.appId()), new String(body));
        request.getHeaders().add(SignUtil.getSignature(), signature);
    }

    private void validateResponse(ClientHttpResponse response) throws IOException {
        Sign sign = SignAssist.get();
        String json = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        String newSignature = SignUtil.signDirect(ldwSignProperties.getApp().get(sign.appId()), json);
        String oldSignature = response.getHeaders().getFirst(SignUtil.getSignature());
        QuickUtil.isFalse(BoolUtil.eq(oldSignature, newSignature), () -> new SkillException(XCode.API_SIGN));
    }
}
