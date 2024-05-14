package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.mod.Tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 树状结构工具包.
 *
 * @author Jas °
 * @date 2021 /10/19 (周二)
 */
public class TreeUtil {
    /**
     * Tree list.
     *
     * @param <T>  the type parameter
     * @param list the list
     * @return the list
     */
    public static <T extends Tree> Collection<T> tree(Collection<T> list) {
        if (BoolUtil.notEmpty(list)) {
            Collection<T> roots = handlerRoot(list, null);
            handleChildren(roots, list);
            return roots;
        }
        return list;
    }

    /**
     * Tree list.
     *
     * @param <T>    the type parameter
     * @param list   the list
     * @param rootId the root id
     * @return the list
     */
    public static <T extends Tree> Collection<T> tree(Collection<T> list, Object rootId) {
        if (BoolUtil.notEmpty(list)) {
            Collection<T> roots = handlerRoot(list, rootId);
            handleChildren(roots, list);
            return roots;
        }
        return list;
    }

    /**
     * Tree map list.
     *
     * @param <T>  the type parameter
     * @param list the list
     * @return the list
     */
    public static <T> Collection<Map> treeMap(Collection<Map> list) {
        if (BoolUtil.notEmpty(list)) {
            Collection<Map> roots = handlerRootByMap(list, Tree.PARENT_ID);
            handleChildrenByMap(roots, list, Tree.ID, Tree.PARENT_ID, Tree.CHILDREN);
            return roots;
        }
        return list;
    }

    /**
     * Tree map list.
     *
     * @param <T>            the type parameter
     * @param list           the list
     * @param customChildren the custom children
     * @return the list
     */
    public static <T> Collection<Map> treeMap(List<Map> list, String customChildren) {
        if (BoolUtil.notEmpty(list)) {
            Collection<Map> roots = handlerRootByMap(list, Tree.PARENT_ID);
            handleChildrenByMap(roots, list, Tree.ID, Tree.PARENT_ID, customChildren);
            return roots;
        }
        return list;
    }

    /**
     * Tree map list.
     *
     * @param <T>            the type parameter
     * @param list           the list
     * @param customChildren the custom children
     * @return the list
     */
    public static <T> Collection<Map> treeMap(Collection<Map> list, String customId, String customParentId, String customChildren) {
        if (BoolUtil.notEmpty(list)) {
            Collection<Map> root = handlerRootByMap(list, Tree.PARENT_ID);
            handleChildrenByMap(root, list, customId, customParentId, customChildren);
            return root;
        }
        return list;
    }

    private static <T extends Tree> Collection<T> handlerRoot(Collection<T> list, Object rootId) {
        Collection<T> tree = new ArrayList();
        list.forEach(obj -> {
            Object parentId = obj.getParentId();
            if (BoolUtil.isNull(parentId) || BoolUtil.eq(parentId, obj.getId()) || BoolUtil.eq(parentId, rootId)) {
                tree.add(obj);
            }
        });
        return tree;
    }

    private static Collection<Map> handlerRootByMap(Collection<Map> list, String parentId) {
        Collection<Map> tree = new ArrayList();
        list.forEach(map -> {
            Object o = map.get(parentId);
            if (BoolUtil.isNull(o) || BoolUtil.eq(parentId, map.get(Tree.ID))) {
                tree.add(map);
            }
        });
        return tree;
    }

    private static <T extends Tree> void handleChildren(Collection<T> roots, Collection<T> source) {
        if (BoolUtil.notEmpty(roots)) {
            roots.forEach(root -> {
                List<T> children = source.stream().filter(s -> BoolUtil.eq(s.getParentId(), root.getId()) && !BoolUtil.eq(s.getParentId(), s.getId())).collect(Collectors.toList());
                root.setChildren(children);
                handleChildren(children, source);
            });
        }
    }

    private static void handleChildrenByMap(Collection<Map> roots, Collection<Map> source, String id, String parentId, String children) {
        if (BoolUtil.notEmpty(roots)) {
            roots.forEach(root -> {
                List<Map> childrenList = source.stream().filter(s -> BoolUtil.eq(s.get(parentId), root.get(id)) && !BoolUtil.eq(s.get(parentId), s.get(id))).collect(Collectors.toList());
                root.put(children, childrenList);
                handleChildrenByMap(childrenList, source, id, parentId, children);
            });
        }
    }
}
