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
    public static String getCurrentThreadStackTrace() {
        return getThreadStackTrace(Thread.currentThread());
    }

    /**
     * 获取指定线程的完整堆栈日志原文.
     *
     * @param thread the thread
     * @return the thread stack trace
     */
    public static String getThreadStackTrace(Thread thread) {

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
            sb.append("\tat ")
                    .append(element)
                    .append('\n');
        }

        return sb.toString();
    }
}
