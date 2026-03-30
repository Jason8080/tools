package cn.gmlee.tools.es.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 通用持久化工具
 * <p>
 * Spring Data Elasticsearch 5+ 已移除基于 {@code org.elasticsearch.index.query.QueryBuilder} 的仓储查询 API；
 * 分页与原生查询请使用 {@code ElasticsearchOperations} + {@code NativeQuery}/{@code CriteriaQuery} 等在业务层实现。
 * </p>
 *
 * @param <T>  the type parameter
 * @param <ID> the type parameter
 * @author Jas °
 * @date 2021 /3/6 (周六)
 */
public interface BaseRepository<T, ID> extends ElasticsearchRepository<T, ID> {
    /**
     * 批量更新.
     *
     * @param t the t
     */
    default void update(T t) {
        // TODO: 暂无实现
    }

    /**
     * 主键更新.
     *
     * @param t the t
     */
    default void updateById(T t) {
        save(t);
    }

    /**
     * 批量更新 (均根据主键更新).
     *
     * @param ts the ts
     */
    default void updateBatchById(java.util.Collection<T> ts) {
        saveAll(ts);
    }
}