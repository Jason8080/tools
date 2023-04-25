package cn.gmlee.tools.base.mod;

import cn.gmlee.tools.base.util.BoolUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 通用请求实体
 * <p>
 * 该对象是简单的分页响应对象, 支持重写;
 * 不提供: 其实记录、结束记录、上一页、下一页、第一页、最后一页、是否有上一页、是否有下一页等
 * </p>
 * <p>
 * 重写可参考mybatis分页插件: pagehelper
 * private int pageNum;
 * private int pageSize;
 * private int size;
 * private long startRow;
 * private long endRow;
 * private int pages;
 * private int prePage;
 * private int nextPage;
 * private boolean isFirstPage;
 * private boolean isLastPage;
 * private boolean hasPreviousPage;
 * private boolean hasNextPage;
 * private int navigatePages;
 * private int[] navigatepageNums;
 * private int navigateFirstPage;
 * private int navigateLastPage;
 * <p>
 * protected long total;
 * protected List<T> list;
 * </p>
 *
 * @param <T> the type parameter
 * @author Jas °
 * @date 2020 /9/8 (周二)
 */
@Getter
@Setter
@ToString
public class PageResponse<T> extends PageRequest implements Serializable {
    /**
     * The Total.
     */
    public Long total;
    /**
     * The List.
     */
    public Collection<T> list;

    public PageResponse() {
    }

    /**
     * Instantiates a new Page response.
     *
     * @param page  the page
     * @param total the total
     */
    public PageResponse(PageRequest page, Long total) {
        this.current = page.current;
        this.size = page.size;
        this.total = total;
    }

    /**
     * Instantiates a new Page response.
     *
     * @param current the current
     * @param size    the size
     * @param total   the total
     */
    public PageResponse(Integer current, Integer size, Long total) {
        this.current = current;
        this.size = size;
        this.total = total;
    }

    /**
     * Instantiates a new Page response.
     *
     * @param page  the page
     * @param total the total
     * @param list  the list
     */
    public PageResponse(PageRequest page, Long total, Collection<T> list) {
        this.current = page.current;
        this.size = page.size;
        this.total = total;
        this.list = list;
    }

    /**
     * Instantiates a new Page response.
     *
     * @param current the current
     * @param size    the size
     * @param total   the total
     * @param list    the list
     */
    public PageResponse(Integer current, Integer size, Long total, Collection<T> list) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.list = list;
    }

    /**
     * Of page response.
     *
     * @param <T>     the type parameter
     * @param current the current
     * @param size    the size
     * @param total   the total
     * @param list    the list
     * @return the page response
     */
    public static <T> PageResponse<T> of(Integer current, Integer size, Long total, Collection<T> list) {
        return new PageResponse(current, size, total, list);
    }

    /**
     * Of page response.
     *
     * @param <T>   the type parameter
     * @param pr    the pr
     * @param total the total
     * @param list  the list
     * @return the page response
     */
    public static <T> PageResponse<T> of(PageRequest pr, Long total, Collection<T> list) {
        return new PageResponse(pr, total, list);
    }

    /**
     * 代码分页.
     *
     * @param <T>  数据泛型
     * @param page 分页请求
     * @param c    全部数据(未分页的全部数据)
     * @return 当前分页对象 page response
     */
    public static <T> PageResponse<T> paging(PageRequest page, Collection<T> c) {
        PageResponse<T> response = new PageResponse(page, 0L);
        if (BoolUtil.notEmpty(c)) {
            response.setTotal(Long.valueOf(c.size()));
            List<T> onePage = new ArrayList();
            // 如果数据量少直接返回
            if(c.size() <= page.size){
                onePage.addAll(c);
                response.setList(onePage);
            }
            Iterator<T> it = c.iterator();
            int index = 0;
            while (it.hasNext()) {
                T next = it.next();
                if ((index >= page.current * page.size) && (index < page.current * page.size + page.size)) {
                    onePage.add(next);
                }
                index++;
            }
            response.setList(onePage);
        }
        return response;
    }

    /**
     * Empty page response.
     *
     * @param <T> the type parameter
     * @return the page response
     */
    public static <T> PageResponse<T> empty() {
        return new PageResponse(1, 5, 0L);
    }

    /**
     * Empty page response.
     *
     * @param <T> the type parameter
     * @param pr  the pr
     * @return the page response
     */
    public static <T> PageResponse<T> empty(PageRequest pr) {
        return new PageResponse(pr, 0L);
    }

    /**
     * Clone page response.
     *
     * @param <T> the type parameter
     * @param pr  the pr
     * @return the page response
     */
    public static <T> PageResponse<T> clone(PageResponse<T> pr) {
        if(BoolUtil.notEmpty(pr.list)){
            List<T> list = pr.list.stream().collect(Collectors.toList());
            return new PageResponse(pr, pr.total, list);
        }
        return new PageResponse(pr, pr.total, pr.list);
    }

    /**
     * Clone page response.
     *
     * @param <T>  the type parameter
     * @param pr   the pr
     * @param list the list
     * @return the page response
     */
    public static <T> PageResponse<T> clone(PageResponse pr, Collection<T> list) {
        return new PageResponse(pr, pr.total, list);
    }
}
