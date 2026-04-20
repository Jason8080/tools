package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.WebUtil;

import java.util.HashMap;
import java.util.Map;

public class WebTests {

    public static void main(String[] args) {
        String url = "http://www.google.com";
        Map<String, Object> params = new HashMap<>();
        params.put("type", "sys");
        params.put("code", "third_party_api");
        String newUrl = WebUtil.addParam(url, params);
        System.out.println(newUrl);
    }
}
