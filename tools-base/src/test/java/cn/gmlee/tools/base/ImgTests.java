package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.ImgUtil;
import cn.gmlee.tools.base.util.StreamUtil;
import org.junit.Test;

import java.io.*;

/**
 * 测试图片工具类
 *
 * @author Jas°
 * @date 2020/10/10 (周六)
 */
public class ImgTests {

    @Test
    public void test2Img() throws Exception {
        InputStream in = new FileInputStream(new File("C:\\Users\\Administrator\\Desktop\\沙箱\\实验室\\a.jpg"));
        ByteArrayOutputStream bs = StreamUtil.toStream(in);
        byte[] bytes = bs.toByteArray();
        String base64 = ImgUtil.bytes2base64(bytes);
        System.out.println("=====================================");
        System.out.println(base64);
        System.out.println("=====================================");
        byte[] newBytes = ImgUtil.base642bytes(base64);
        FileOutputStream out = new FileOutputStream(new File("C:\\Users\\Administrator\\Desktop\\沙箱\\实验室\\b.jpg"));
        out.write(newBytes);
        out.flush();
        out.close();
    }
}
