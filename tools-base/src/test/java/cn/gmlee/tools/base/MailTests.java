package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.MailUtil;
import cn.gmlee.tools.base.util.UrlUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试Http工具类
 *
 * @author Jas °
 * @date 2020 /10/10 (周六)
 */
public class MailTests {
    /**
     * The Username.
     */
// "cs.sys@gmlee.cn", "GM2019"
//    String username = "cs.sys@gmlee.cn";
    /**
     * The Password.
     */
//    String password = "GM2019";
    /**
     * The To.
     */
    String username = "gmleemail@163.com";
    String password = "QQ123456";
    String[] to = {"xiaoku13141@163.com", "569284276@qq.com"};
    /**
     * The Cc.
     */
    String[] cc = {"xiaoku13141@163.com", "569284276@qq.com"};
    /**
     * The Bcc.
     */
    String[] bcc = {"xiaoku13141@163.com", "569284276@qq.com"};
    /**
     * The Replies.
     */
    String[] replies = {"xiaoku13141@163.com", "569284276@qq.com"};
    /**
     * The Images.
     */
    String[] images = {"C:\\Users\\gmlee\\Desktop\\实验大厅\\头像.jpg"};
    /**
     * The Files.
     */
    String[] files = {"C:\\Users\\gmlee\\Desktop\\实验大厅\\file.conf", "C:\\Users\\gmlee\\Desktop\\实验大厅\\铃声大气.mp3", "C:\\Users\\gmlee\\Desktop\\实验大厅\\留资列表2022-04-02.zip"};
    /**
     * The Executor service.
     */
    ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * Test send.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testSend() throws IOException {
        MailUtil.Mail mail = MailUtil.build(username, password);
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> mail.send("好久不见, 你还好吗?", "或许你已经不记得我了吧", to));
        }
        System.in.read();
    }

    /**
     * Test send replies.
     */
    @Test
    public void testSendReplies() {
        MailUtil.Mail mail = MailUtil.build(username, password);
        mail.send(replies, "好久不见, 你还好吗?", "不理我吗", to);
    }

    /**
     * Test send images.
     */
    @Test
    public void testSendImages() {
//        MailUtil.Mail mail = MailUtil.build(username, password);
        MailUtil.Mail mail = MailUtil.build(username, password, "smtp.exmail.qq.com", "smtp", 25);
        mail.send("你真的狠心", "<image src='cid:0'/><br/>我去<br/><a href='https://www.baidu.com'><image src='cid:1'/></a>", images, files, to, null, null);
    }

    /**
     * Test tencent.
     */
    @Test
    public void testTencent() {
        MailUtil.Mail mail = MailUtil.build(username, password);
        mail.send("杨帆启航", "你真的狠心", "xiaoku13141@163.com");
    }

    /**
     * Test lee.
     */
    @Test
    public void testLee() {
        MailUtil.Mail mail = MailUtil.build("cs.sys@gmlee.cn", "GM2019", "smtp.exmail.qq.com", "smtp", 465);
        mail.send("杨帆启航", "你真的狠心6666", "xiaoku13141@163.com");
    }

    /**
     * Test byte 2 string.
     *
     * @throws Exception the exception
     */
    @Test
    public void testByte2String() throws Exception {
        byte[] bytes = {
                60, 104, 116, 109, 108, 62, 13, 10, 60, 104, 101, 97, 100, 62, 60, 116, 105, 116, 108, 101, 62, 52, 48, 52, 32, 78, 111, 116, 32, 70, 111, 117, 110, 100, 60, 47, 116, 105, 116, 108, 101, 62, 60, 47, 104, 101, 97, 100, 62, 13, 10, 60, 98, 111, 100, 121, 62, 13, 10, 60, 99, 101, 110, 116, 101, 114, 62, 60, 104, 49, 62, 52, 48, 52, 32, 78, 111, 116, 32, 70, 111, 117, 110, 100, 60, 47, 104, 49, 62, 60, 47, 99, 101, 110, 116, 101, 114, 62, 13, 10, 60, 104, 114, 62, 60, 99, 101, 110, 116, 101, 114, 62, 111, 112, 101, 110, 114, 101, 115, 116, 121, 60, 47, 99, 101, 110, 116, 101, 114, 62, 13, 10, 60, 47, 98, 111, 100, 121, 62, 13, 10, 60, 47, 104, 116, 109, 108, 62, 13, 10};
        System.out.println(new String(bytes));
    }

    /**
     * Test url decode.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUrlDecode() throws Exception {
        String url = "http://gmlee.cn/ggg+64131.html";
        String encode = UrlUtil.encodeOnce(url);
        System.out.println(encode);
        System.out.println(UrlUtil.decode(encode));
    }

    /**
     * Lc代码测试.
     *
     * @throws Exception the exception
     */
    @Test
    public void testLc() throws Exception {
        String role, str;
        role = "ab*ccd";
        str = "ab666ccd";
        long start = System.currentTimeMillis();
        System.out.println("执行结果:" + solveProblems(role, str));
        long end = System.currentTimeMillis();
        System.out.println("算法耗时:" + (end - start) + "(ms)");
    }

    /**
     * 正则匹配算法.
     *
     * @param role the role
     * @param str  the str
     * @return the int
     */
    public static List<String> solveProblems(String role, String str) {
        // 符合正则的字符串
        List<String> all = new ArrayList();
        char[] chars = str.toCharArray();
        // 后一字符
        char last = ' ';
        StringBuilder sb = new StringBuilder();
        // 遍历规则
        for (int i = chars.length - 1; i > -1; i--) {
            char c = chars[i];
            if(find(role, c, last, i, sb)){
                all.add(sb.toString());
                sb.delete(0, sb.length());
            }
            last = c;
        }
        return all;
    }

    private static boolean find(String role, char current, char last, int i, StringBuilder sb) {
        // 偏移量
        int offset = 0;
        return false;
    }
}
