package cn.gmlee.tools.base;

import java.io.FileOutputStream;

/**
 * @author Jas°
 * @date 2021/3/20 (周六)
 */
public class StreamTests {
    public static void main(String[] args) throws Exception {
        FileOutputStream out = new FileOutputStream("C:\\Users\\gmlee\\Desktop\\rb.txt");
        System.out.println(out);
    }
}
