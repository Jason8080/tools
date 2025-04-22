package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.server.impl.ApplicationServer;
import cn.gmlee.tools.base.builder.KvBuilder;
import cn.gmlee.tools.base.enums.Regex;
import cn.gmlee.tools.base.util.RegexUtil;
import cn.gmlee.tools.base.util.TimerUtil;
import io.reactivex.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class ApplicationWlTests {

    @Autowired
    private ApplicationServer applicationServer;

    private static final String user = "提取净重";

    // 1911610483048722433
    private static final String image1 =
            "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E6%A8%AA%E5%90%91%E9%94%99%E8%A1%8C%E7%A3%85%E5%8D%951.png?Expires=11745231268&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=vv4Zt%2BzO9L2oWJ%2BAbs%2BkIECQBcQ%3D";

    // 1914661974219173889
    private static final String image2 =
            "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E6%A8%AA%E5%90%91%E9%94%99%E8%A1%8C%E7%A3%85%E5%8D%952.png?Expires=11745325968&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=0ZEFXyXFR1P5SRVlUbzEqkhh%2B18%3D";

    // 1914661974219173889
    private static final String image3 =
            "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E5%8D%B8%E8%BD%A6%E7%A3%85%E5%8D%95_11.jpg?Expires=11745326657&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=8j1bfZ4fvT5e8LPPlKgsYPw6Eso%3D";


    @Test
    public void testAsk(){
        TimerUtil.print();
        Flowable<String> ask = applicationServer.ask(user, KvBuilder.array("image", image3));
        StringBuilder sb = new StringBuilder();
        ask.blockingForEach(x -> sb.append(x));
        System.out.println(sb);
        System.out.println(RegexUtil.last(sb.toString(), Regex.NUMBER.regex, true));
        TimerUtil.print();
    }

}
