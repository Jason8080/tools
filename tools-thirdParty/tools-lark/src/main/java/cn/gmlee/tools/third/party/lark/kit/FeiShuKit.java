package cn.gmlee.tools.third.party.lark.kit;

import cn.gmlee.tools.base.builder.MapBuilder;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import com.lark.oapi.Client;
import com.lark.oapi.core.enums.BaseUrlEnum;
import com.lark.oapi.service.im.v1.model.CreateMessageReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 飞书工具.
 */
@Slf4j
public class FeiShuKit {

    private static final Client defaultClient = Client.newBuilder("cli_a8624fd47914100c","6Pb5IJgpIt92anHV2Yng4baMycVjuRZP")
            .openBaseUrl(BaseUrlEnum.FeiShu) // 设置域名，默认为飞书
            .requestTimeout(3, TimeUnit.SECONDS) // 设置httpclient 超时时间，默认永不超时
            .logReqAtDebug(true) // 在 debug 模式下会打印 http 请求和响应的 headers、body 等信息。
            .build();

    /**
     * 发送模板消息.
     *
     * @param templateId the template id
     * @param variables  the variables
     * @param chatIds    the chat ids
     */
    public static void send(String templateId, Map<String, Serializable> variables, String... chatIds) {
        // 构建变量集合
        Map<String, Object> variablesMap = new HashMap<>(2);
        Map<String, Object> dataMap = new HashMap<>(2);
        variablesMap.put("type", "template");
        variablesMap.put("data", dataMap);
        dataMap.put("template_id", templateId);
        dataMap.put("template_variable", variables);

        // 创建请求对象
        CreateMessageReq req = CreateMessageReq.newBuilder()
                .receiveIdType("chat_id")
                .createMessageReqBody(CreateMessageReqBody.newBuilder()
                        .receiveId(String.join(",", chatIds))
                        .msgType("interactive")
                        .content(JsonUtil.toJson(variablesMap))
                        .build())
                .build();

        // 发起请求
        CreateMessageResp resp = ExceptionUtil.sandbox(() -> defaultClient.im().v1().message().create(req));

        if(resp == null){
            return;
        }

        // 处理服务端错误
        if(!resp.success()) {
            log.warn("code:{},msg:{},reqId:{}, resp:{}", resp.getCode(), resp.getMsg(), resp.getRequestId(), new String(resp.getRawResponse().getBody()));
        }
    }
}
