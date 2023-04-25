package cn.gmlee.tools.mybatis.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Collection;

/**
 * 通用mybatis-plus批量持久化操作工具
 *
 * @param <T> the type parameter
 * @author Jas °
 * @date 2021 /3/6 (周六)
 */
public interface BatchMapper<T> extends BaseMapper<T> {
    /**
     * 插入多条记录.
     *
     * @param ts the ts
     * @return the int
     */
    default long insertBatch(Collection<T> ts) {
        if (ts != null) {
            return ts.stream().map(t -> this.insert(t)).count();
        }
        return 0;
    }

    /**
     * 更新多条记录 (均根据主键).
     *
     * @param ts the ts
     * @return the long
     */
    default long updateBatchById(Collection<T> ts) {
        if (ts != null) {
            return ts.stream().map(t -> this.updateById(t)).count();
        }
        return 0;
    }
}
