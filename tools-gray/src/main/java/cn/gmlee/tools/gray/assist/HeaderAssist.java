package cn.gmlee.tools.gray.assist;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.gray.conf.GrayProperties;
import org.springframework.http.HttpHeaders;

import java.util.List;

/**
 * The type Header assist.
 */
public class HeaderAssist {
    /**
     * Gets version.
     *
     * @param headers    the headers
     * @param properties the properties
     * @return the version
     */
    public static String getVersion(HttpHeaders headers, GrayProperties properties) {
        List<String> heads = headers.get(properties.getHead());
        return BoolUtil.isEmpty(heads) ? null : heads.get(0);
    }
}
