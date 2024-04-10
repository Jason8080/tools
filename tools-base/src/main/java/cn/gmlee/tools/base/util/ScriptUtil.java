package cn.gmlee.tools.base.util;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * 脚本执行工具.
 */
public class ScriptUtil {

    private static ScriptEngineManager manager = new ScriptEngineManager();

    private static ScriptEngine scriptEngine = manager.getEngineByName("JavaScript");


    /**
     * 执行并返回结果.
     *
     * @param <T>    the type parameter
     * @param script the script
     * @return the t
     */
    public static <T> T eval(String script) {
        return ExceptionUtil.sandbox(() -> (T) scriptEngine.eval(script), false);
    }

    /**
     * 执行并返回结果 (异常时打印日志).
     *
     * @param <T>    the type parameter
     * @param script the script
     * @param print  the print
     * @return the t
     */
    public static <T> T eval(String script, boolean print) {
        return ExceptionUtil.sandbox(() -> (T) scriptEngine.eval(script), print);
    }
}
