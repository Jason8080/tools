package cn.gmlee.tools.base;

import cn.gmlee.tools.base.builder.KvBuilder;
import cn.gmlee.tools.base.mod.HttpResult;
import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.mod.Login;
import cn.gmlee.tools.base.util.*;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * 测试Http工具类
 *
 * @author Jas°
 * @date 2020/10/10 (周六)
 */
public class HttpTests {

    private static final String APP_ID ="wx2b294c5820908199";
    private static final String SECRET ="3627e26cd0fa7b22abc97f41efc11d1d";

    @Test
    public void testForm(){
        Map map = new HashMap();
        map.put("key", "key1");
        map.put("val", "val1");
        map.put("name", "name2");
        map.put("desc", "desc2");
        Kv<String, String>[] kvs = KvBuilder.array(HttpUtil.CONTENT_TYPE, HttpUtil.DATA_HEADER);
        HttpResult httpResult = HttpUtil.post("http://localhost:9002/test/testHttp?current=2&size=8", map, kvs);
        System.out.println(httpResult.byteResponseBody2String());
    }

    @Test
    public void testUcForm(){
        Long timestamp = TimeUtil.getCurrentTimestampSecond();
        String sortParams = String.format("%s_aget_list_mdepart_t%sappid%smodifiedTime%spage%spageSize%sversion%s%s",
                "762cb3b9ad5606b11e7c87c401939790",
                timestamp, 100191, 943891200, 1, 100, "1.0",
                "762cb3b9ad5606b11e7c87c401939790");
        Map map = new TreeMap();
        map.put("modifiedTime", 943891200);
        map.put("page", 1);
        map.put("pageSize", 100);
        map.put("appid", 100191);
        map.put("_t", timestamp);
        map.put("version", "1.0");
        map.put("_sign", Md5Util.encode(sortParams).toUpperCase());
        Kv<String, String>[] kvs = KvBuilder.array(HttpUtil.CONTENT_TYPE, HttpUtil.DATA_HEADER);
        HttpResult httpResult = HttpUtil.post("https://ucenter-stg.gmlee.cn/index.php?_m=depart&_a=get_list", map, kvs);
        System.out.println(httpResult.byteResponseBody2String());
    }


    @Test
    public void testLogin(){
        Map map = new HashMap();
        map.put("loginName", "Jas");
        map.put("loginPass", "123");
        HttpResult httpResult = HttpUtil.post("https://uc-dev.gmlee.cn/login/loginSso", map);
        System.out.println(httpResult.byteResponseBody2String());
    }

    @Test
    public void testEncode(){
        Map map = new HashMap();
        map.put("clientName", "dataapi");
        map.put("token", "3E073F8227ED975E");
        List<String> phones = new ArrayList();
        phones.add("13257617382");
        map.put("phoneList", phones);
        HttpResult httpResult = HttpUtil.post("http://192.168.107.154:8888/datacloud/phone/batch/encrypt", map);
        System.out.println(httpResult.byteResponseBody2String());
    }

    @Test
    public void testGet(){
        HttpResult httpResult = HttpUtil.get("https://api.weixin.qq.com/cgi-bin/token?" +
                "grant_type=client_credential&appid=" + APP_ID + "&secret=" + SECRET);
        System.out.println(httpResult.jsonResponseBody2bean(Map.class));
    }

    @Test
    public void testAsyncGet() throws Exception {
        String url = "https://www.baidu.com/";
        AsyncHttpUtil.get(url, x -> System.out.println(x.byteResponseBody2String()));
        Thread.sleep(10000);
    }

    @Test
    public void testPost2() throws Exception {
        String url = "http://audit-log-stg.gmlee.cn/log/login";
        Login login = new Login();
        login.setToken("999999999999999999999999999999~~");
        HttpResult httpResult = HttpUtil.post(url, login);
        System.out.println(httpResult.byteResponseBody2String());
    }

    @Test
    public void testPost() throws Exception {
//        Map<String, String> token = HttpUtil.get("https://api.weixin.qq.com/cgi-bin/token?" +
//                "grant_type=client_credential&appid="+ APP_ID +"&secret="+ SECRET, Map.class);
//        String access_token = token.get("access_token");
        String access_token = "38_pty_FiJKpvDPewwEIL3k7A1rtM75xTiEvltXuZs1nLn8R6eJdrXp3-yVHC24s1oMcSxAFWiMWJvhJ1y-1TiLJXEo6tG9DpKRxYE_SLa57doJfEuk1sJtyMMztOMIJ_Er1rFCJ_6nh0EFz20uHRRaACAKUU";
        Map<String, Object> params = new HashMap();
        params.put("scene", "hello");
        OutputStream out = new FileOutputStream(new File("C:\\Users\\DELL\\Desktop\\实验室\\a.jpg"));
        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + access_token;
        HttpUtil.post(url, params, out);
    }


    @Test
    public void testUc() throws Exception {
        HttpResult httpResult = HttpUtil.post("https://reach-stg.gmlee.cn/third-party/uc/push", "{\"loginName\":\"alias.chen\", \"loginPass\":\"121312321\"}");
        System.out.println(httpResult.byteResponseBody2String());
    }


    @Test
    public void testOrder() throws Exception {
        HttpResult httpResult = HttpUtil.post("http://oms-pre.gmlee.cn/api/order/addfull", "{\"loginName\":\"alias.chen\", \"loginPass\":\"121312321\"}");
        System.out.println(httpResult.byteResponseBody2String());
    }

    @Test
    public void testUrl() throws IOException {
        URL url = new URL("https://reach-stg.gmlee.cn/third-party/uc/push");
        URLConnection con = url.openConnection();
        con.setDoOutput(true);
        con.setRequestProperty("accept", "*/*");
        con.setRequestProperty("connection", "Keep-Alive");
        OutputStream out = con.getOutputStream();
        out.write("{\"loginName\":\"alias.chen\", \"loginPass\":\"121312321\"}".getBytes());
        out.flush();
        con.connect();
        InputStream in = con.getInputStream();
        System.out.println(StreamUtil.toString(in));
    }

}
