package cn.gmlee.tools.base.mod;

import java.io.Serializable;
import java.util.Collection;

/**
 * 树.
 *
 * @param <T> the type parameter
 * @author Jas °
 * @date 2021 /10/19 (周二)
 */
public interface Tree<T extends Tree> extends Serializable {
    /**
     * Gets id.
     *
     * @return the id
     */
    Long getId();

    /**
     * Sets id.
     *
     * @param id the id
     */
    void setId(Long id);

    /**
     * Gets parent id.
     *
     * @return the parent id
     */
    Long getParentId();

    /**
     * Sets parent id.
     *
     * @param parentId the parent id
     */
    void setParentId(Long parentId);

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
