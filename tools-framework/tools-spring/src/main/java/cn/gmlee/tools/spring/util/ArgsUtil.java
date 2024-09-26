package cn.gmlee.tools.spring.util;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;

/**
 * 参数列表处理工具.
 */
public class ArgsUtil {

    private static final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    /**
     * Get parameter names string [ ].
     *
     * @param method the method
     * @return the string [ ]
     */
    public static String[] getParameterNames(Method method){
        if(method == null){
            return new String[0];
        }
        return discoverer.getParameterNames(method);
    }

}
