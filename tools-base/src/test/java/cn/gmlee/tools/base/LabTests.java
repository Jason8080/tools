package cn.gmlee.tools.base;

import org.junit.Test;

/**
 * 实验室.
 *
 * @author Jas°
 * @date 2021/8/19 (周四)
 */
public class LabTests {

    @Test
    public void reverse() throws Exception {
        System.out.println(reverse("123456789".toCharArray()));
    }









    public char[] reverse(char... cs) {
        int n = cs.length - 1;
        for (int j = (n-1) >> 1; j >= 0; j--) {
            int k = n - j;
            char cj = cs[j];
            char ck = cs[k];
            cs[j] = ck;
            cs[k] = cj;
        }
        return cs;
    }
}
