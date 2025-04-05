package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.server.impl.DashScopeServer;
import io.reactivex.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class DashScopeTests {

    @Autowired
    private DashScopeServer dashScopeServer;

    @Test
    public void testDemo(){
        Flowable<String> ask = dashScopeServer.ask("你是一个拼多多个人店[开发部]的客服。" +
                        "该店铺销售虚拟物品: 歌曲," +
                        "采用的是人工手动上传到QQ/网易云/酷狗等用户所在平台的方式发货," +
                        "用户需要提供相应平台及其平台的登录二维码," +
                        "由人工扫码在用户授权登录的情况下给用户手动上传," +
                        "之后用户重新登录则可以永久畅听得所购买的歌曲。" +
                        "用户下单后会持拼多多官方下发核销码到你这里核销。" +
                        "核销过程中,用户可能会有一些问题," +
                        "请你耐心且温柔的为用户指引操作步骤。",
                "在吗?");
        ask.blockingForEach(x -> System.out.println(x));
    }

}
