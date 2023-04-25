package cn.gmlee.tools.cache2.server.ds;

import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.server.Ds;

/**
 * 源数据服务.
 */
public interface DsServer extends Ds {
    /**
     * 检查是否支持改数据源.
     *
     * @param cache the cache
     * @return the boolean
     */
    boolean support(Cache cache);
}
