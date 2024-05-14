package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.mod.Tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 树状结构工具包.
 *
 * @author Jas °
 * @date 2021 /10/19 (周二)
 */
public class TreeUtil {

    /**
     * 扁平化处理.
     *
     * @param <T>  the type parameter
     * @param tree the tree
     * @return the list
     */
    private static <T extends Tree> List<T> flatten(Collection<T> tree) {
        return tree.stream().flatMap(TreeUtil::flatten).collect(Collectors.toList());
    }

    /**
     * Flatten stream.
     *
     * @param <T>  the type parameter
     * @param tree the tree
     * @return the stream
     */
    private static <T extends Tree> Stream<T> flatten(T tree) {
        return Stream.concat(Stream.of(tree), tree.getChildren().stream().flatMap((x) -> flatten((T) x)));
    }

    /**
     * List collection.
     *
     * @param <T>  the type parameter
     * @param tree the tree
     * @return the collection
     */
    public static <T extends Tree> Collection<T> list(Collection<T> tree) {
        if (BoolUtil.notEmpty(tree)) {
            return flatten(tree);
        }
        return tree;
    }

    // -----------------------------------------------------------------------------------------------------------------

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
     * @param rootIds the root id
     * @return the list
     */
    public static <T extends Tree> Collection<T> tree(Collection<T> list, Object... rootIds) {
        if (BoolUtil.notEmpty(list)) {
            Collection<T> roots = handlerRoot(list, rootIds);
            handleChildren(roots, list);
            return roots;
        }
        return list;
    }

    // -----------------------------------------------------------------------------------------------------------------

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
     * @param customId       the custom id
     * @param customParentId the custom parent id
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

    // -----------------------------------------------------------------------------------------------------------------

    private static <T extends Tree> Collection<T> handlerRoot(Collection<T> list, Object... rootIds) {
        Collection<T> tree = new ArrayList();
        list.forEach(obj -> {
            Object parentId = obj.getParentId();
            if (BoolUtil.isNull(parentId) || BoolUtil.eq(parentId, obj.getId()) || BoolUtil.containOne(rootIds, parentId)) {
                tree.add(obj);
            }
        });
        return tree;
    }

    private static Collection<Map> handlerRootByMap(Collection<Map> list, String... parentIds) {
        Collection<Map> tree = new ArrayList();
        list.forEach(map -> {
            for (String parentId : parentIds) {
                Object o = map.get(parentId);
                if (BoolUtil.isNull(o) || BoolUtil.eq(parentId, map.get(Tree.ID))) {
                    tree.add(map);
                }
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
