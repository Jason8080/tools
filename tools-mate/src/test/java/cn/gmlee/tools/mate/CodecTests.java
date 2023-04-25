package cn.gmlee.tools.mate;

import cn.gmlee.tools.base.util.SnUtil;
import cn.gmlee.tools.mate.assist.FutureAssist;
import org.junit.Test;

/**
 * The type Codec tests.
 */
public class CodecTests {

    /**
     * 结论: 不支持模糊搜索.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        String encode;
        encode = SnUtil.encode("123", "TOOLS");
        System.out.println(encode);
        encode = SnUtil.encode("1", "TOOLS");
        System.out.println(encode);
        encode = SnUtil.encode("2", "TOOLS");
        System.out.println(encode);
        encode = SnUtil.encode("3", "TOOLS");
        System.out.println(encode);
        encode = SnUtil.encode("23", "TOOLS");
        System.out.println(encode);
        encode = SnUtil.encode("01234", "TOOLS");
        System.out.println(encode);
    }


    @Test
    public void testFuture(){
        System.out.println("------------" + Thread.currentThread().getId());
        String s = FutureAssist.supplyAsync(() -> hello());
        System.out.println("------------" + Thread.currentThread().getId());
        System.out.println(s);
    }

    private String hello() {
        System.out.println("------------" + Thread.currentThread().getId());
        return "你好";
    }
}
