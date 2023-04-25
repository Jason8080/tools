package cn.gmlee.tools.base;

import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.ImgUtil;
import cn.gmlee.tools.base.util.VcUtil;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * 测试图片工具类
 *
 * @author Jas°
 * @date 2020/10/10 (周六)
 */
public class VcTests {

    @Test
    public void testVc() throws Exception {
        for (int i=0; i<100; i++) {
            OutputStream out = new FileOutputStream("C:\\Users\\gmlee\\Desktop\\实验大厅\\vc.jpg");
            Kv<String, String> kv = VcUtil.generateBase64(100, 50);
            byte[] bytes = ImgUtil.base642bytes(kv.getVal());
            out.write(bytes);
            out.flush();
            out.close();
            Thread.sleep(500);
        }
    }
}
