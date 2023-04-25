package cn.gmlee.tools.third.party.tencent.util;

import cn.gmlee.tools.base.mod.HttpResult;
import cn.gmlee.tools.base.util.HttpUtil;
import cn.gmlee.tools.third.party.tencent.model.res.ErrorInfo;
import cn.gmlee.tools.third.party.tencent.model.res.IndustryInfo;
import cn.gmlee.tools.third.party.tencent.model.res.MsgInfo;
import cn.gmlee.tools.third.party.tencent.model.req.SendAppletCmRequest;
import cn.gmlee.tools.third.party.tencent.model.req.SendServiceSuRequest;
import cn.gmlee.tools.third.party.tencent.model.req.SendServiceTmRequest;

/**
 * 通用微信消息推送工具
 *
 * @author Jas °
 * @date 2021 /2/22 (周一)
 */
public class MsgUtil {
    private static final String APPLET_CUSTOMER_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=%s";
    private static final String SERVICE_TEMPLATE_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s";
    private static final String SERVICE_SINGLE_USE_URL = "https://api.weixin.qq.com/cgi-bin/message/template/subscribe?access_token=%s";
    private static final String GET_SERVICE_TEMPLATE_URL = "https://api.weixin.qq.com/cgi-bin/template/get_industry?access_token=%s";

    /**
     * 小程序发送客服消息(CM: CustomMessage, 非统一消息).
     * <p>
     * <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/customer-message/customerServiceMessage.send.html">详情请查看官网地址</a>
     * </p>
     *
     * @param accessToken the access token
     * @param openId      the open id
     * @param content     the content
     * @return the error info
     */
    public static ErrorInfo appletCmSendText(String accessToken, String openId, String content) {
        SendAppletCmRequest message = new SendAppletCmRequest();
        message.setTouser(openId);
        message.setText(new SendAppletCmRequest.Text(content));
        HttpResult httpResult = HttpUtil.post(String.format(APPLET_CUSTOMER_MESSAGE_URL, accessToken), message);
        return httpResult.jsonResponseBody2bean(ErrorInfo.class);
    }

    /**
     * 公众号获取模板消息所在行业.
     * <p>
     * <a href="https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Template_Message_Interface.html">详情请查看官网地址</a>
     * </p>
     *
     * @param accessToken the access token
     * @return the msg info
     */
    public static IndustryInfo getServiceTm(String accessToken) {
        HttpResult httpResult = HttpUtil.get(String.format(GET_SERVICE_TEMPLATE_URL, accessToken));
        return httpResult.jsonResponseBody2bean(IndustryInfo.class);
    }

    /**
     * 公众号发送模板消息(TM: TemplateMessage).
     * <p>
     * <a href="https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Template_Message_Interface.html">详情请查看官网地址</a>
     * </p>
     *
     * @param accessToken the access token
     * @param req         the req
     * @return the msg info
     */
    public static MsgInfo serviceTmSend(String accessToken, SendServiceTmRequest req) {
        HttpResult httpResult = HttpUtil.post(String.format(SERVICE_TEMPLATE_MESSAGE_URL, accessToken), req);
        return httpResult.jsonResponseBody2bean(MsgInfo.class);
    }

    /**
     * 公众号发送一次性订阅消息(TM: TemplateMessage).
     * <p>
     * <a href="https://developers.weixin.qq.com/doc/offiaccount/Message_Management/One-time_subscription_info.html">详情请查看官网地址</a>
     * </p>
     *
     * @param accessToken the access token
     * @param req         the req
     * @return the msg info
     */
    public static ErrorInfo serviceSuSend(String accessToken, SendServiceSuRequest req) {
        HttpResult httpResult = HttpUtil.post(String.format(SERVICE_SINGLE_USE_URL, accessToken), req);
        return httpResult.jsonResponseBody2bean(ErrorInfo.class);
    }
}
