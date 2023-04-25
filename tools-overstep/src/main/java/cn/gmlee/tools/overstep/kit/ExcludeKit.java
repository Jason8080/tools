package cn.gmlee.tools.overstep.kit;

import cn.gmlee.tools.base.util.UrlUtil;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.overstep.config.SnProperties;

/**
 * 接口路径排除工具.
 */
public class ExcludeKit {

    /**
     * 排除模式检查.
     *
     * @param snProperties
     * @return
     */
    public static boolean check(SnProperties snProperties, Object field) {
        return exclude(snProperties) && exist(snProperties, field);
    }

    /**
     * 字段存在检查.
     * @param snProperties
     * @param field
     * @return
     */
    public static boolean exist(SnProperties snProperties, Object field) {
        return snProperties.getFields().contains(field);
    }


    /**
     * 排除模式检查.
     * @param snProperties
     * @return
     */
    private static boolean exclude(SnProperties snProperties){
        boolean ok = UrlUtil.matchOne(snProperties.getUrls(), WebUtil.getCurrentRelPath());
        return snProperties.getExclude() ? !ok : ok;
    }
}
