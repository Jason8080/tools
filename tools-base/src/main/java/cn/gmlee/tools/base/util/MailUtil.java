package cn.gmlee.tools.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

/**
 * 邮件工具类.
 *
 * @author Jas °
 */
public class MailUtil {
    private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);

    private static volatile String ALIAS = "TOOLS";

    private static final String CHARSET = "utf-8";

    /**
     * 设置发件人别名.
     *
     * @param alias the alias
     */
    public static void setAlias(String alias) {
        MailUtil.ALIAS = alias;
    }

    private static final Properties properties = new Properties();

    /**
     * 初始化邮件工具.
     *
     * @param username the username
     * @param password the password
     * @return the mail
     */
    public static Mail build(String username, String password) {
        return build(username, password, null, null, null, null, null, null);
    }

    /**
     * 初始化邮件工具(设置服务器).
     *
     * @param username the username
     * @param password the password
     * @param host     the host
     * @return the mail
     */
    public static Mail build(String username, String password, String host) {
        return build(username, password, host, null, null, null, null, null);
    }

    /**
     * 初始化邮件工具(设置协议).
     *
     * @param username the username
     * @param password the password
     * @param host     the host
     * @param protocol the protocol
     * @return the mail
     */
    public static Mail build(String username, String password, String host, String protocol) {
        return build(username, password, host, protocol, null, null, null, null);
    }

    /**
     * 初始化邮件工具(设置端口).
     *
     * @param username the username
     * @param password the password
     * @param host     the host
     * @param protocol the protocol
     * @param port     the port
     * @return the mail
     */
    public static Mail build(String username, String password, String host, String protocol, Integer port) {
        return build(username, password, host, protocol, port, null, null, null);
    }

    /**
     * 初始化邮件工具(全量定制).
     *
     * @param username the username
     * @param password the password
     * @param host     the host
     * @param protocol the protocol
     * @param port     the port
     * @param auth     the auth
     * @param debug    the debug
     * @param ssl      the ssl
     * @return mail mail
     */
    public static Mail build(String username, String password, String host, String protocol, Integer port, Boolean auth, Boolean debug, Boolean ssl) {
        AssertUtil.contain(username, "@", String.format("邮箱地址非法"));
        properties.setProperty("mail.debug", NullUtil.get(debug, true).toString());
        properties.setProperty("mail.mime.splitlongparameters", "false");
        properties.setProperty("mail.transport.protocol", NullUtil.get(protocol, "smtp"));
        properties.setProperty("mail.smtp.user", username);
        properties.setProperty("mail.smtp.pass", password);
        properties.setProperty("mail.smtp.host", NullUtil.get(host, "smtp.163.com"));
        properties.setProperty("mail.smtp.port", NullUtil.get(port, 25).toString());
        properties.setProperty("mail.smtp.auth", NullUtil.get(auth, true).toString());
        properties.setProperty("mail.smtp.ssl.enable", NullUtil.get(ssl, false).toString());
        if (!BoolUtil.eq(properties.getProperty("mail.smtp.port"), "25")) {
            properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        Session session = getSession();
        return new Mail(session, getTransport(session, null));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The type Mail.
     */
    public static class Mail {
        private Session session;
        private Transport transport;

        /**
         * Instantiates a new Mail.
         *
         * @param session   the session
         * @param transport the transport
         */
        public Mail(Session session, Transport transport) {
            this.session = session;
            this.transport = transport;
        }

        /**
         * 发送纯文本内容.
         *
         * @param title     标题
         * @param content   内容
         * @param receivers 收件人
         * @return the boolean
         */
        public boolean send(String title, String content, String... receivers) {
            AssertUtil.notEmpty(receivers, String.format("收件人是空"));
            return send(null, title, content, null, null, receivers, null, null);
        }

        /**
         * 发送纯文本并接收答复.
         *
         * @param replies   回复
         * @param title     标题
         * @param content   内容
         * @param receivers 收件人
         * @return the boolean
         */
        public boolean send(String[] replies, String title, String content, String... receivers) {
            AssertUtil.notEmpty(receivers, String.format("收件人是空"));
            return send(replies, title, content, null, null, receivers, null, null);
        }

        /**
         * 发送无回复多媒体邮件.
         *
         * @param title    标题
         * @param content  内容
         * @param images   图片地址
         * @param attaches 附件地址
         * @param toArray  收件人
         * @param ccArray  抄送人
         * @param bccArray 密送人
         * @return the boolean
         */
        public boolean send(String title, String content, String[] images, String[] attaches, String[] toArray, String[] ccArray, String[] bccArray) {
            AssertUtil.notEmpty(toArray, String.format("收件人是空"));
            return send(null, title, content, images, attaches, toArray, ccArray, bccArray);
        }


        /**
         * Send boolean.
         *
         * @param title    标题
         * @param content  内容
         * @param attaches 附件地址以;号分割
         * @param tos      收件人地址以;号分割
         * @return the boolean
         */
        public boolean send(String title, String content, String attaches, String tos) {
            AssertUtil.notEmpty(attaches, String.format("附件是空", tos));
            AssertUtil.notEmpty(tos, String.format("收件人是空"));
            return send(null, title, content, null, attaches.split(";"), tos.split(";"), null, null);
        }

        /**
         * Send boolean.
         *
         * @param title    标题
         * @param content  内容
         * @param attaches 附件地址
         * @param tos      收件人 ; 号分割
         * @param ccs      抄送人 ; 号分割
         * @param bcc      密送人 ; 号分割
         * @return the boolean
         */
        public boolean send(String title, String content, String attaches, String tos, String ccs, String bcc) {
            AssertUtil.notEmpty(attaches, String.format("附件是空"));
            AssertUtil.notEmpty(tos, String.format("收件人是空"));
            AssertUtil.notEmpty(ccs, String.format("抄送人是空"));
            AssertUtil.notEmpty(bcc, String.format("密送人是空"));
            return send(null, title, content, null, attaches.split(";"), tos.split(";"), ccs.split(";"), bcc.split(";"));
        }

        /**
         * Send boolean.
         *
         * @param title    标题
         * @param content  内容
         * @param images   图片地址 ; 号分割
         * @param attaches 附件地址 ; 号分割
         * @param tos      收件人地址 ; 号分割
         * @return the boolean
         */
        public boolean send(String title, String content, String images, String attaches, String tos) {
            AssertUtil.notEmpty(attaches, String.format("图片是空", images));
            AssertUtil.notEmpty(attaches, String.format("附件是空", tos));
            AssertUtil.notEmpty(tos, String.format("收件人是空"));
            return send(null, title, content, images.split(";"), attaches.split(";"), tos.split(";"), null, null);
        }

        /**
         * Send boolean.
         *
         * @param title    标题
         * @param content  内容
         * @param images   图片地址
         * @param attaches 附件地址
         * @param tos      收件人
         * @return the boolean
         */
        public boolean send(String title, String content, String[] images, String[] attaches, String[] tos) {
            AssertUtil.notEmpty(tos, String.format("收件人是空"));
            return send(null, title, content, images, attaches, tos, null, null);
        }


        /**
         * 发送带内嵌图片、附件、多收件人(显示邮箱姓名)、邮件优先级、阅读回执的完整的HTML邮件
         *
         * @param replies  回复人
         * @param title    标题
         * @param content  HTML内容
         * @param images   图片绝对路径
         * @param attaches 附件绝对路径
         * @param toArray  收件人
         * @param ccArray  抄送人
         * @param bccArray 密送人
         * @return the boolean
         */
        public boolean send(String[] replies, String title, String content, String[] images, String[] attaches, String[] toArray, String[] ccArray, String[] bccArray) {
            try {
                // 创建一封邮件
                MimeMessage message = getMimeMessage(replies, title, toArray, ccArray, bccArray);

                // 创建混合型载体
                message.setContent(getMimeMultipart(content, images, attaches));

                // 保存邮件
                long start = System.currentTimeMillis();
                message.saveChanges();
                logger.debug("保存耗时".concat(String.valueOf(System.currentTimeMillis() - start)));

                // 发送邮件
                start = System.currentTimeMillis();

                // 发送邮件
                transport = getTransport(session, transport);
                transport.sendMessage(message, message.getAllRecipients());
                logger.debug("发送耗时".concat(String.valueOf(System.currentTimeMillis() - start)));
            } catch (Exception e) {
                logger.error("邮件发送失败", e);
                return false;
            }
            return true;
        }

        private MimeMessage getMimeMessage(String[] replies, String title, String[] toArray, String[] ccArray, String[] bccArray) throws MessagingException, UnsupportedEncodingException {
            MimeMessage message = new MimeMessage(session);
            // 设置主题
            message.setSubject(title);
            // 设置发送人
            String sender = session.getProperty("mail.smtp.user");
            message.setFrom(new InternetAddress(sender, ALIAS, CHARSET));
            // 设置收件人
            message.setRecipients(Message.RecipientType.TO, getAddress(toArray));
            // 设置抄送
            // 此处固定抄送给自己一份 (能解决不少软异常)
            message.setRecipients(Message.RecipientType.CC, getAddressInclude(sender, ccArray));
            // 设置密送
            message.setRecipients(Message.RecipientType.BCC, getAddress(bccArray));
            // 设置发送时间
            message.setSentDate(new Date());
            // 设置优先级(1:紧急   3:普通    5:低)
            message.setHeader("X-Priority", "1");
            if (BoolUtil.notEmpty(replies)) {
                // 设置回复人(收件人回复此邮件时,默认收件人)
                message.setReplyTo(getAddress(replies));
                // 要求阅读回执(收件人阅读邮件时会提示回复发件人,表明邮件已收到,并已阅读)
                message.setHeader("Disposition-Notification-To", getReplies(replies));
            }
            return message;
        }

        private String getReplies(String[] replies) {
            StringBuilder sb = new StringBuilder();
            for (String replier : replies) {
                sb.append(replier);
                sb.append(";");
            }
            return sb.toString();
        }

        private MimeMultipart getMimeMultipart(String content, String[] images, String[] attaches) throws MessagingException, UnsupportedEncodingException {
            MimeMultipart mimeMultipart = new MimeMultipart();
            mimeMultipart.setSubType("mixed");
            // 图文内容
            MimeMultipart imageText = new MimeMultipart();
            MimeBodyPart it = new MimeBodyPart();
            it.setContent(imageText);
            // 文本内容: ------------------------------------------------------------------------------------------------
            MimeBodyPart text = new MimeBodyPart();
            text.setContent(content, "text/html;charset=".concat(CHARSET));
            imageText.addBodyPart(text);
            // 图片内容: ------------------------------------------------------------------------------------------------
            if (BoolUtil.notEmpty(images)) {
                for (Integer i = 0; i < images.length; i++) {
                    // 创建载体
                    MimeBodyPart img = new MimeBodyPart();
                    // 读取图片
                    DataSource source = new FileDataSource(images[i]);
                    DataHandler data = new DataHandler(source);
                    img.setDataHandler(data);
                    img.setContentID(i.toString());
                    imageText.addBodyPart(img);
                }
            }
            // 添加到邮件当中
            imageText.setSubType("related");
            mimeMultipart.addBodyPart(it);
            // 附件内容: ------------------------------------------------------------------------------------------------
            if (BoolUtil.notEmpty(attaches)) {
                for (String attach : attaches) {
                    // 创建载体
                    MimeBodyPart part = new MimeBodyPart();
                    // 读取附件
                    DataSource source = new FileDataSource(attach);
                    DataHandler data = new DataHandler(source);
                    part.setDataHandler(data);
                    part.setFileName(MimeUtility.encodeText(source.getName(), CHARSET, "B"));
                    // 添加到邮件当中
                    mimeMultipart.addBodyPart(part);
                }
            }
            return mimeMultipart;
        }

        private Address[] getAddress(String... s) throws UnsupportedEncodingException {
            if (BoolUtil.notEmpty(s)) {
                Address[] addresses = new InternetAddress[s.length];
                for (int i = 0; i < s.length; i++) {
                    addresses[i] = new InternetAddress(s[i], s[i], CHARSET);
                }
                return addresses;
            }
            return new InternetAddress[0];
        }

        /**
         * 专门用于抄送给自己
         *
         * @param sender
         * @param s
         * @return
         * @throws UnsupportedEncodingException
         */
        private Address[] getAddressInclude(String sender, String... s) throws UnsupportedEncodingException {
            if (BoolUtil.notEmpty(s)) {
                String[] ccArray = new String[s.length + 1];
                ccArray[ccArray.length - 1] = sender;
                getAddress(ccArray);
            }
            return new InternetAddress[]{new InternetAddress(sender, sender, CHARSET)};
        }


        /**
         * 将邮件内容生成eml文件
         *
         * @param message 邮件内容
         * @return the file
         * @throws MessagingException the messaging exception
         * @throws IOException        the io exception
         */
        private static File writeTo(Message message) throws MessagingException, IOException {
            File file = new File(MimeUtility.decodeText(message.getSubject()) + ".eml");
            message.writeTo(new FileOutputStream(file));
            return file;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static Authenticator getAuthenticator() {
        String username = properties.getProperty("mail.smtp.user");
        String password = properties.getProperty("mail.smtp.pass");
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    private static Session getSession() {
        return Session.getDefaultInstance(properties, getAuthenticator());
    }

    private static Transport getTransport(Session session, Transport transport) {
        String username = properties.getProperty("mail.smtp.user");
        String password = properties.getProperty("mail.smtp.pass");
        String host = properties.getProperty("mail.smtp.host");
        String port = properties.getProperty("mail.smtp.port");
        if (transport != null && transport.isConnected()) {
            return transport;
        }
        try {
            Transport newTransport = session.getTransport();
            newTransport.connect(host, Integer.valueOf(port), username, password);
            return newTransport;
        } catch (MessagingException e) {
            e.printStackTrace();
            logger.error(String.format("连接邮件服务器%s出错", host), e);
        }
        return null;
    }
}
