package cn.gmlee.tools.base;

import cn.gmlee.tools.base.enums.XState;
import cn.gmlee.tools.base.mod.Login;
import cn.gmlee.tools.base.mod.UserInfo;
import cn.gmlee.tools.base.util.IdUtil;
import cn.gmlee.tools.base.util.JsonUtil;

import java.time.LocalDateTime;

/**
 * .
 *
 * @author Jas°
 * @date 2021/8/5 (周四)
 */
public class LoginTests {
    public static void main(String[] args) {
        login();
    }

    private static void login() {
        Login login = new Login();
        login.setToken(IdUtil.uuidReplaceUpperCase());
        login.setLoginIp("127.0.0.1");
        login.setLoginTime(LocalDateTime.now());
        login.setLoginUrl("https://oms.gmlee.com/login");
        login.setLon("114.067058");
        login.setLat("22.574195");
        login.setAppId("10123");
        login.setOpenId("wx2fKo330j541fhe11");
        login.setUid(667300L);
        login.setUsername("Jas.lee");
        login.setUserType("僵尸");
        login.setCity("新疆");
        login.setPortrait("http://api.map.baidu.com/lbsapi/getpoint/Images/logo.gif");
        login.setStatus(XState.YES.code);
        UserInfo userInfo = new UserInfo();
        login.setUser(userInfo);
        userInfo.setGender(1);
        userInfo.setRealName("李*星");
        userInfo.setMobile("188******3232");
        userInfo.setIdCard("2918***********1471");
        System.out.println(JsonUtil.format(login));
    }
}
