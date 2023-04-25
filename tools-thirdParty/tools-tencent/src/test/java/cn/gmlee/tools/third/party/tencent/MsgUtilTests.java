package cn.gmlee.tools.third.party.tencent;

import cn.gmlee.tools.base.util.TimeUtil;
import cn.gmlee.tools.third.party.tencent.model.res.ErrorInfo;
import cn.gmlee.tools.third.party.tencent.model.res.IndustryInfo;
import cn.gmlee.tools.third.party.tencent.model.res.MsgInfo;
import cn.gmlee.tools.third.party.tencent.model.res.TokenInfo;
import cn.gmlee.tools.third.party.tencent.util.MsgUtil;
import cn.gmlee.tools.third.party.tencent.util.WxUtil;
import cn.gmlee.tools.third.party.tencent.model.req.SendServiceSuRequest;
import cn.gmlee.tools.third.party.tencent.model.req.SendServiceTmRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Jas°
 * @date 2021/2/22 (周一)
 */
public class MsgUtilTests {


    private static final String APP_ID = "wx018695992c5db6d1";
    private static final String SECRET = "a12cbc77685caf97494348d0073c797d";

    private static String openId = "o0cw55mq8q73c5rkBPJtLrsr9iDk";
    private static String templateId = "A_kP4OXBTtNlPuVEhQ2WnGWTxgCzI5v8sHzWEXlTSE4";

    public static void main(String[] args) {
        serviceSuSend();
    }

    public static void serviceSuSend() {
        TokenInfo tokenInfo = WxUtil.getAccessToken(APP_ID, SECRET);
        SendServiceSuRequest request = new SendServiceSuRequest();
        request.setTouser(openId);
        request.setTemplate_id(templateId);
        Map<String, SendServiceTmRequest.Text> data = new LinkedHashMap();
        data.put("first", new SendServiceTmRequest.Text("感谢评价"));
        data.put("keyword1", new SendServiceTmRequest.Text("X8******79"));
        data.put("keyword2", new SendServiceTmRequest.Text("5 星"));
        data.put("keyword3", new SendServiceTmRequest.Text(TimeUtil.getCurrentDatetime()));
        data.put("remark", new SendServiceTmRequest.Text("祝您生活愉快!"));
        request.setData(data);
        request.setScene("1");
        request.setTitle("雷猴");
        ErrorInfo errorInfo = MsgUtil.serviceSuSend(tokenInfo.getAccess_token(), request);
        System.out.println(errorInfo);
    }

    public static void serviceTmSend() {
        TokenInfo tokenInfo = WxUtil.getAccessToken(APP_ID, SECRET);
        SendServiceTmRequest request = new SendServiceTmRequest();
        request.setTouser(openId);
        request.setTemplate_id(templateId);
        Map<String, SendServiceTmRequest.Text> data = new LinkedHashMap();
        data.put("first", new SendServiceTmRequest.Text("感谢评价"));
        data.put("keyword1", new SendServiceTmRequest.Text("X8******79"));
        data.put("keyword2", new SendServiceTmRequest.Text("5 星"));
        data.put("keyword3", new SendServiceTmRequest.Text(TimeUtil.getCurrentDatetime()));
        data.put("remark", new SendServiceTmRequest.Text("祝您生活愉快!"));
        request.setData(data);
        MsgInfo msgInfo = MsgUtil.serviceTmSend(tokenInfo.getAccess_token(), request);
        System.out.println(msgInfo);
    }

    public static void getServiceTm() {
        TokenInfo tokenInfo = WxUtil.getAccessToken(APP_ID, SECRET);
        IndustryInfo industryInfo = MsgUtil.getServiceTm(tokenInfo.getAccess_token());
        System.out.println(industryInfo);
    }
}
