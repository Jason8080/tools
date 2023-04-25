package cn.gmlee.tools.base.util;

/**
 * Java Virtual Machine 工具 .
 *
 * @author Jas °
 * @date 2021 /8/12 (周四)
 */
public class JvmUtil {

    /**
     * 获取当前线程的全部堆栈信息.
     *
     * @return the all msg
     */
    public static String getAllMsg(String... msg) {
        StringBuffer sb = new StringBuffer();
        if(BoolUtil.notEmpty(msg)){
            sb.append(": ");
            sb.append(String.join(" ", msg));
        }
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < elements.length; i++) {
            sb.append("\r\n");
            sb.append("\t");
            sb.append("at ");
            sb.append(elements[i].getClassName());
            sb.append(".");
            sb.append(elements[i].getMethodName());
            sb.append("(");
            sb.append(elements[i].getClassName());
            sb.append(":");
            sb.append(elements[i].getLineNumber());
            sb.append(")");
        }
        sb.insert(0, getStarter(elements[elements.length - 1]));
        return sb.toString();
    }

    private static String getStarter(StackTraceElement element) {
        StringBuffer sb = new StringBuffer();
        sb.append(element.getClassName());
        sb.append(": ");
        sb.append("[");
        sb.append(element.getMethodName());
        sb.append(":");
        sb.append(element.getLineNumber());
        sb.append("/");
        sb.append(Thread.currentThread().getName());
        sb.append(":");
        sb.append(Thread.currentThread().getId());
        sb.append("]");
        return sb.toString();
    }
}
