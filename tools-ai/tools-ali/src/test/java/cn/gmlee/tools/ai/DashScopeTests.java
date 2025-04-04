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
    public void testText(){
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

    @Test
    public void testImage(){
        Flowable<String> ask = dashScopeServer.askImage("你是一个拼多多个人店[开发部]的客服。" +
                        "该店铺销售虚拟物品: 歌曲," +
                        "采用的是人工手动上传到QQ/网易云/酷狗等用户所在平台的方式发货," +
                        "用户需要提供相应平台及其平台的登录二维码," +
                        "由人工扫码在用户授权登录的情况下给用户手动上传," +
                        "之后用户重新登录则可以永久畅听得所购买的歌曲。" +
                        "用户下单后会持拼多多官方下发核销码到你这里核销。" +
                        "核销过程中,用户可能会有一些问题," +
                        "请你耐心且温柔的为用户指引操作步骤。",
                "这个怎么用?", "https://img2.baidu.com/it/u=4136464951,3224683733&fm=253&fmt=auto&app=138&f=JPEG?w=600&h=500");
        ask.blockingForEach(x -> System.out.println(x));
    }

    @Test
    public void testImages(){
        Flowable<String> ask = dashScopeServer.askImages("你是一个拼多多个人店[开发部]的客服。" +
                        "该店铺销售虚拟物品: 歌曲," +
                        "采用的是人工手动上传到QQ/网易云/酷狗等用户所在平台的方式发货," +
                        "用户需要提供相应平台及其平台的登录二维码," +
                        "由人工扫码在用户授权登录的情况下给用户手动上传," +
                        "之后用户重新登录则可以永久畅听得所购买的歌曲。" +
                        "用户下单后会持拼多多官方下发核销码到你这里核销。" +
                        "核销过程中,用户可能会有一些问题," +
                        "请你耐心且温柔的为用户指引操作步骤。",
                "这些图片里分别是什么内容?",
                "https://qcloud.dpfile.com/pc/OMj1_yv-CN2NU56Wt3MRRHeLZJ2qhTgBFJOHlmKh0RYopzl7UKxf251v2HlD_Lu0.jpg",
                "http://img1.baidu.com/it/u=2884132715,401430863&fm=253&app=138&f=JPEG?w=800&h=1067"
        );
        ask.blockingForEach(x -> System.out.println(x));
    }

    @Test
    public void testAudio(){
        Flowable<String> ask = dashScopeServer.askAudio("你是一个拼多多个人店[开发部]的客服。" +
                        "该店铺销售虚拟物品: 歌曲," +
                        "采用的是人工手动上传到QQ/网易云/酷狗等用户所在平台的方式发货," +
                        "用户需要提供相应平台及其平台的登录二维码," +
                        "由人工扫码在用户授权登录的情况下给用户手动上传," +
                        "之后用户重新登录则可以永久畅听得所购买的歌曲。" +
                        "用户下单后会持拼多多官方下发核销码到你这里核销。" +
                        "核销过程中,用户可能会有一些问题," +
                        "请你耐心且温柔的为用户指引操作步骤。",
                "这首歌的名字是?", "https://img.tukuppt.com/newpreview_music/00/14/45/5e4e3b09b963f82212.mp3");
        ask.blockingForEach(x -> System.out.println(x));
    }

    @Test
    public void testVideo(){
        Flowable<String> ask = dashScopeServer.askVideo("你是一个拼多多个人店[开发部]的客服。" +
                        "该店铺销售虚拟物品: 歌曲," +
                        "采用的是人工手动上传到QQ/网易云/酷狗等用户所在平台的方式发货," +
                        "用户需要提供相应平台及其平台的登录二维码," +
                        "由人工扫码在用户授权登录的情况下给用户手动上传," +
                        "之后用户重新登录则可以永久畅听得所购买的歌曲。" +
                        "用户下单后会持拼多多官方下发核销码到你这里核销。" +
                        "核销过程中,用户可能会有一些问题," +
                        "请你耐心且温柔的为用户指引操作步骤。",
                "你看到什么又听到什么?", "https://img.tukuppt.com/video_show/2418175/00/01/51/5b44e0e3ad42a.mp4");
        ask.blockingForEach(x -> System.out.println(x));
    }

}
