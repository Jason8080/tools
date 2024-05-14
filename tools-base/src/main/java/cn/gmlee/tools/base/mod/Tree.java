package cn.gmlee.tools.base.mod;

import java.io.Serializable;
import java.util.Collection;

/**
 * 树.
 *
 * @param <T>  类型
 * @param <ID> 主键类型
 * @author Jas °
 * @date 2021 /10/19 (周二)
 */
public interface Tree<T, ID> extends Serializable {
    String ID = "id";
    String PARENT_ID = "parentId";
    String CHILDREN = "children";
    /**
     * Gets id.
     *
     * @return the id
     */
    ID getId();

    /**
     * Sets id.
     *
     * @param id the id
     */
    void setId(ID id);

    /**
     * Gets parent id.
     *
     * @return the parent id
     */
    ID getParentId();

    /**
     * Sets parent id.
     *
     * @param parentId the parent id
     */
    void setParentId(ID parentId);

    /**
     * Gets children.
     *
     * @return the children
     */
    Collection<T> getChildren();

    /**
     * Sets children.
     *
     * @param children the children
     */
    void setChildren(Collection<T> children);
}
