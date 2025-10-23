package cn.gmlee.tools.base.util;

/**
 * Java Virtual Machine 工具 .
 *
 * @author Jas °
 */
public class JvmUtil {

    /**
     * 获取当前线程的全部堆栈信息.
     *
     * @param msg the msg
     * @return the all msg
     */
    public static String getAllMsg(String... msg) {
        return getAllMsg(Thread.currentThread(), msg);
    }

    /**
     * 获取指定线程的全部堆栈信息.
     *
     * @param thread the thread
     * @param msg    the msg
     * @return the all msg
     */
    public static String getAllMsg(Thread thread, String... msg) {
        StringBuilder sb = new StringBuilder();
        if (BoolUtil.notEmpty(msg)) {
            sb.append(String.format("---------- %s ----------", String.join(" ", msg)));
        }
        sb.append(getThreadStackTrace(thread));
        return sb.toString();
    }

    /**
     * 获取当前线程的完整堆栈日志原文.
     *
     * @return the current thread stack trace
     */
    public static String getCurrentThreadStackTrace(String... ignores) {
        return getThreadStackTrace(Thread.currentThread(), ignores);
    }

    /**
     * 获取指定线程的完整堆栈日志原文.
     *
     * @param thread  the thread
     * @param ignores the ignores
     * @return the thread stack trace
     */
    public static String getThreadStackTrace(Thread thread, String... ignores) {

        if (thread == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(512);

        sb.append("\r\nThread: ")
                .append(thread.getName())
                .append(" [id=")
                .append(thread.getId())
                .append(", state=")
                .append(thread.getState())
                .append("]\n");

        for (StackTraceElement element : thread.getStackTrace()) {

            if (ignore(element, ignores)) {
                continue;
            }

            sb.append("\tat ")
                    .append(element)
                    .append('\n');
        }

        return sb.toString();
    }

    private static boolean ignore(StackTraceElement element, String... ignores) {
        if (BoolUtil.isEmpty(ignores)) {
            return false;
        }
        for (String ignore : ignores) {
            int index = ignore.indexOf('#');
            String className = index > -1 ? ignore.substring(0, index) : ignore;
            String methodName = index > -1 ? ignore.substring(index + 1) : "";
            if (element.getClassName().startsWith(className) && element.getMethodName().contains(methodName)) {
                return true;
            }
        }
        return false;
    }
}
