package cn.gmlee.tools.agent.assist;

import cn.gmlee.tools.agent.mod.Watcher;
import org.springframework.cglib.reflect.FastClass;

/**
 * The type Original assist.
 */
public class OriginalAssist {
    /**
     * 解析原生对象和方法.
     *
     * @param watcher the watcher
     */
    public static void parserFastClassOriginalObjectAndMethod(Watcher watcher) {
        Object originalObj = watcher.getOriginalObj();
        if (!(originalObj instanceof FastClass)) {
            return;
        }
        Class<?> clazz = ((FastClass) originalObj).getJavaClass();
        Object[] args = watcher.getArgs();
        if (args.length != 3) {
            return;
        }
        if (args[0] instanceof Integer && args[2] instanceof Object[]) {
            Integer idx = (Integer) args[0];
            Object obj = args[1];
            Object[] arg = (Object[]) args[2];
            watcher.setOriginalObj(obj);
            watcher.setOriginalArgs(arg);
            watcher.setOriginalMethod(clazz.getMethods()[idx]);
        }
    }
}
