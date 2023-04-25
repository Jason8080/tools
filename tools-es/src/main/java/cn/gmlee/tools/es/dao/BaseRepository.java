package cn.gmlee.tools.es.dao;

import cn.gmlee.tools.base.mod.PageRequest;
import cn.gmlee.tools.base.mod.PageResponse;
import cn.gmlee.tools.base.util.BeanUtil;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * 通用持久化工具
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
    default void updateBatchById(Collection<T> ts) {
        saveAll(ts);
    }

    /**
     * 分页查询.
     *
     * @param t       the t
     * @param request the request
     * @param orders  the orders
     * @return page response
     */
    default PageResponse<T> selectPage(T t, PageRequest request, Sort.Order... orders) {
        QueryBuilder all = getQueryBuilder(t);
        Page<T> page = this.search(all, org.springframework.data.domain.PageRequest.of(request.current, request.size, Sort.by(orders)));
        return new PageResponse(request, page.getTotalElements(), page.getContent());
    }

    /**
     * Gets query builder.
     *
     * @param t the t
     * @return the query builder
     */
    default QueryBuilder getQueryBuilder(T t) {
        Map<String, Object> map = BeanUtil.convert(t, Map.class);
        BoolQueryBuilder all = QueryBuilders.boolQuery();
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> next = it.next();
            String key = next.getKey();
            Object value = next.getValue();
            all.must(QueryBuilders.termQuery(key, value));
        }
        return all;
    }
}
