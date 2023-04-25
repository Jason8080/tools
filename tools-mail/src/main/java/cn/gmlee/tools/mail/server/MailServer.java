package cn.gmlee.tools.mail.server;

import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.MailUtil;
import cn.gmlee.tools.base.util.ThreadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * 邮件发送服务.
 *
 * @author Jas °
 * @date 2021 /7/27 (周二)
 */
@Component
public class MailServer {
    private MailUtil.Mail mail;

    private ThreadUtil.Pool pool;

    @Value("${spring.profiles.active:unknown}")
    private String profile;

    /**
     * Instantiates a new Mail server.
     *
     * @param username   the username
     * @param password   the password
     * @param threadPool the thread pool
     */
    public MailServer(
            @Value("${tools.mail.username:请输入邮箱地址}") String username,
            @Value("${tools.mail.password:请输入密码}") String password,
            @Value("${tools.mail.threadPool:5}") Integer threadPool,
            @Value("${tools.mail.host:smtp.163.com}") String host,
            @Value("${tools.mail.port:25}") Integer port,
            @Value("${tools.mail.protocol:smtp}") String protocol,
            @Value("${tools.mail.auth:true}") Boolean auth,
            @Value("${tools.mail.debug:false}") Boolean debug,
            @Value("${tools.mail.ssl:false}") Boolean ssl
    ) {
        this.pool = ThreadUtil.getFixedPool(threadPool);
        pool.execute(() -> MailUtil.build(username, password, host, protocol, port, auth, debug, ssl), x -> this.mail = x);
    }

    /**
     * 发送邮件.
     *
     * @param title      标题
     * @param content    内容
     * @param recipients 接收人
     */
    public void send(String title, String content, String... recipients) {
        pool.execute(() -> mail.send(title, content, recipients));
    }

    /**
     * 发送邮件.
     * <p>
     * 与sendMail不同的是, 该工具会携带调用栈信息(即: 哪里发的邮件)
     * 以及调用环境信息, 地址信息, 端口信息
     * </p>
     *
     * @param title      the title
     * @param content    the content
     * @param recipients the recipients
     */
    public void log(String title, String content, String... recipients) {
        InetAddress loc = ExceptionUtil.sandbox(() -> InetAddress.getLocalHost());
        String host = ExceptionUtil.sandbox(() -> loc.getHostName());
        String ip = ExceptionUtil.sandbox(() -> loc.getHostAddress());
        String msg = "<center>"
                + "\t 主机: ".concat(host)
                + "\t 地址: ".concat(ip)
                + "\t 环境: ".concat(getEnv())
                + "</center>"
                + "<hr/>"
                + content;
        pool.execute(() -> mail.send(title, msg, recipients));
    }

    private String getEnv() {
        return System.getProperty("GM.env", profile);
    }
}
