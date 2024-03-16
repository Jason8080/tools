package cn.gmlee.tools.base;

import cn.gmlee.tools.base.enums.Function;
import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.base.util.ExceptionUtil;
import org.junit.Test;

public class ExceptionUtilTests {


    @Test
    public void main() {
        R<String> result = ExceptionUtil.sandbox(
                (Function.Zero2r<R>) () -> R.OK.newly(1/0),
                (Function.P2r<Throwable, R>) (Throwable e) -> R.FAIL.newly(e.getMessage())
        );
        System.out.println(result);
    }
}
