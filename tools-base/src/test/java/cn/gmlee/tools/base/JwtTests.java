package cn.gmlee.tools.base;

import cn.gmlee.tools.base.define.Payload;
import cn.gmlee.tools.base.util.JwtUtil;
import cn.gmlee.tools.base.util.MacUtil;
import org.junit.Test;

/**
 * .
 *
 * @author Jas°
 * @date 2021/9/16 (周四)
 */
public class JwtTests {

    @Test
    public void testMac() throws Exception {
        String encode = MacUtil.encode(MacUtil.HS256, "666", "123");
        System.out.println(encode);
    }

    @Test
    public void testJwt() throws Exception {
        // 编码
        String jwt = JwtUtil.generate(1L, "123");
        System.out.println(jwt);
        // 解码
        Payload payload = JwtUtil.get(jwt, "123");
        System.out.println(payload.getUid());
    }
}
