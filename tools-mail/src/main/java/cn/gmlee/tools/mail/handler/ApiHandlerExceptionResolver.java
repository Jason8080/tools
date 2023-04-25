package cn.gmlee.tools.mail.handler;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.ThreadUtil;
import cn.gmlee.tools.mail.server.MailServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Jas°
 * @date 2021/7/27 (周二)
 */
public class ApiHandlerExceptionResolver implements HandlerExceptionResolver {

    private ThreadUtil.Pool pool = ThreadUtil.getFixedPool(2);

    @Resource
    private MailServer mailServer;

    @Value("${tools.mail.recipients:xiaoku13141@163.com}")
    private String[] recipients;

    private static final String MAIL_TITLE = "【接口监控】您有新载消息请注意查收!";

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex instanceof SkillException) {
            if (BoolUtil.eq(XCode.UNKNOWN5000.code, ((SkillException) ex).getCode())) {
                send(ex);
            }
        } else {
            send(ex);
        }
        return null;
    }

    private void send(Exception ex) {
        String allMsg = ExceptionUtil.getAllMsg(ex);
        String originMsg = ExceptionUtil.getOriginMsg(ex);
        String msg = "\r\n<center><h1>" + originMsg + "</h1></center><br/>\r\n" +allMsg;
        mailServer.log(MAIL_TITLE, msg, recipients);
    }
}
