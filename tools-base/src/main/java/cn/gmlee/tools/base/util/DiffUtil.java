package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Mark;
import cn.gmlee.tools.base.mod.Diff;

import java.util.*;

/**
 * 差异对比工具.
 */
public class DiffUtil {
    /**
     * Get list.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param target the target
     * @return the list
     */
    public static <T> List<Diff> get(T source, T target) {
        return get("", source, target, 3);
    }

    /**
     * 获取差异列表.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param target the target
     * @param deep   the deep
     * @return the list
     */
    public static <T> List<Diff> get(T source, T target, int deep) {
        return get("", source, target, deep);
    }

    /**
     * 获取差异列表.
     *
     * @param <T>    the type parameter
     * @param item   the item
     * @param source the source
     * @param target the target
     * @param deep   the deep
     * @return the list
     */
    public static <T> List<Diff> get(String item, T source, T target, int deep) {
        // 数据准备
        List<Diff> diffs = new ArrayList<>();
        if (BoolUtil.allNull(source, target)) {
            return diffs;
        }
        // 数据分类
        T t = NullUtil.first(source, target);
        if (t instanceof Collection) {
            diffs.addAll(getCompareList(item, NullUtil.get((Collection) source), NullUtil.get((Collection) target), --deep));
        } else if (t instanceof Map) {
            diffs.addAll(getRecursionMap(item, NullUtil.get((Map) source), NullUtil.get((Map) target), --deep));
        } else if (BoolUtil.isBean(t, Comparable.class)) {
            diffs.addAll(getRecursionMap(item, ClassUtil.generateMap(source), ClassUtil.generateMap(target), --deep));
        }
        return diffs;
    }

    /**
     * 扁平化所有层级的 Diff.
     *
     * @param diffs 原始差异列表
     * @return 扁平化后的列表 ，item 按层级拼接，且 subset 已置空
     */
    public static List<Diff> get(List<Diff> diffs) {
        if (diffs == null) {
            return Collections.emptyList();
        }

        List<Diff> result = new ArrayList<>();

        flattenWithParentItem(diffs, result, null);

        return result;
    }

    /**
     * 递归处理 Diff，拼接 item 并清空 subset.
     *
     * @param currentLevel 当前层级的 Diff 列表
     * @param result       结果收集列表
     * @param parentItem   父层级的 item（用于拼接）
     */
    private static void flattenWithParentItem(List<Diff> currentLevel, List<Diff> result, String parentItem) {

        if (currentLevel == null || currentLevel.isEmpty()) {
            return;
        }

        for (Diff diff : currentLevel) {
            // 拼接当前 item
            String currentItem = (parentItem == null) ? diff.getItem() : parentItem + ">" + diff.getItem();

            diff.setItem(currentItem);

            // 添加到结果
            result.add(diff);

            // 递归处理子集（传入当前拼接后的 item）
            List<Diff> subset = diff.getSubset();

            if (subset != null) {

                flattenWithParentItem(subset, result, currentItem);

                // 清空 subset
                diff.setSubset(null);
            }
        }
    }

    private static Collection<Diff> getCompareList(String item, Collection source, Collection target, int deep) {
        List<Diff> diffs = new ArrayList<>();
        if (BoolUtil.allEmpty(source, target)) {
            return diffs;
        }
        // 对齐处理
        alignmentProcessing(item, source, target, deep, diffs);
        // 排序处理
        sortProcessing(item, source, target, deep, diffs);
        return diffs;
    }

    private static void sortProcessing(String item, Collection source, Collection target, int deep, List<Diff> diffs) {
        // 排序所有元素
        List sourceList = ClassUtil.sortBy(source, Mark.SORT);
        List targetList = ClassUtil.sortBy(target, Mark.SORT);
        // 对比元素差异
        diffs.addAll(getCompareByList(item, sourceList, targetList, deep));
    }

    private static void alignmentProcessing(String item, Collection source, Collection target, int deep, List<Diff> diffs) {
        // 对齐所有元素
        Map<String, Object> sourceMap = ClassUtil.groupBy(source, Mark.ID);
        Map<String, Object> targetMap = ClassUtil.groupBy(target, Mark.ID);
        // 对齐元素差异
        Set<String> publicKeys = CollectionUtil.publicKeys(sourceMap.keySet(), targetMap.keySet());
        getCompareByKeys(item, source, target, publicKeys, sourceMap, targetMap, diffs, deep);
        // 私有元素差异
        Set<String> privateKeys = CollectionUtil.privateKeys(sourceMap.keySet(), targetMap.keySet());
        getCompareByKeys(item, source, target, privateKeys, sourceMap, targetMap, diffs, deep);
    }

    private static Collection<Diff> getCompareByList(String item, Collection source, Collection target, int deep) {
        List<Diff> diffs = new ArrayList<>();
        if (BoolUtil.allEmpty(source, target)) {
            return diffs;
        }
        // 对齐所有元素
        List sourceList = new ArrayList(NullUtil.get(source));
        List targetList = new ArrayList(NullUtil.get(target));
        int size = Math.max(sourceList.size(), targetList.size());
        for (int i = 0; i < size; i++) {
            Object sv = i < sourceList.size() ? sourceList.get(i) : null;
            Object tv = i < targetList.size() ? targetList.get(i) : null;
            Diff diff = new Diff(String.valueOf(i), sv, tv); // 使用原生item
            if (deep >= 0) {
                diff.setSubset(get(diff.getItem(), sv, tv, deep));
            }
            diffs.add(diff);
        }
        return diffs;
    }

    private static void getCompareByKeys(String item, Collection source, Collection target, Set<String> keys, Map<String, Object> sourceMap, Map<String, Object> targetMap, List<Diff> diffs, int deep) {
        if (BoolUtil.isEmpty(keys)) {
            return;
        }
        for (String key : keys) {
            Object s = sourceMap.get(key);
            Object t = targetMap.get(key);
            QuickUtil.notNull(s, source::remove);
            QuickUtil.notNull(t, target::remove);
            diffs.addAll(getRecursionMap(item, ClassUtil.generateMap(s), ClassUtil.generateMap(t), deep));
        }
    }

    private static Collection<Diff> getRecursionMap(String item, Map source, Map target, int deep) {
        List<Diff> diffs = new ArrayList<>();
        if (BoolUtil.allEmpty(source, target)) {
            return diffs;
        }
        // 获取所有字段
        Collection keys = CollectionUtil.merge(source.keySet(), target.keySet());
        for (Object key : new HashSet(keys)) {
            if (key == null) {
                continue;
            }
            Object sv = source.get(key);
            Object tv = target.get(key);
            Diff diff = new Diff(key.toString(), sv, tv); // 使用原生item
            if (deep >= 0) {
                diff.setSubset(get(diff.getItem(), sv, tv, deep));
            }
            diffs.add(diff);
        }
        return diffs;
    }

    /**
     * 对比报告(描述).
     * (10542):
     * {
     * 是否邮寄资料(1：是，0：否):[true]->[true]
     * 是否本人(1：是，0：否):[false]->[false]
     * 是否本人办理:[1]->[1]
     * 过户类型码:[1]->[1]
     * 逻辑删除:[false]->[false]
     * 过户原因类型:[3]->[3]
     * 是否违约(1：是，0：否):[false]->[false]
     * 主键id:[10542]->[10542]
     * 修改人:[0]->[3735]
     * }
     * (10543): ...
     *
     * @param primaryKey      关联主键
     * @param oldsData        旧数据
     * @param newData         新数据
     * @param fieldCommentMap 字段描述
     * @return 差异描述 string
     * @author Jas °
     */
    public static String comparativeReport(String primaryKey, List<Map<String, Object>> oldsData, Map<String, Object> newData, Map<String, String> fieldCommentMap) {
        StringBuilder sb = new StringBuilder();
        if (BoolUtil.notEmpty(newData) && BoolUtil.notEmpty(oldsData)) {
            for (int i = 0; i < oldsData.size(); i++) {
                Map<String, Object> oldData = oldsData.get(i);
                if (BoolUtil.isEmpty(oldData)) {
                    continue;
                }
                if (i != 0) {
                    sb.append(",\r\n");
                }
                sb.append(String.format("(%s):\r\n", oldData.get(primaryKey)));
                sb.append(comparativeReport(oldData, newData, fieldCommentMap));
            }
        }
        return sb.toString();
    }

    /**
     * 对比报告(描述).
     *
     * @param oldData         旧数据
     * @param newData         新数据
     * @param fieldCommentMap 字段描述
     * @return string 对比报告
     */
    public static String comparativeReport(Map<String, Object> oldData, Map<String, Object> newData, Map<String, String> fieldCommentMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        CollectionUtil.publicKeys(oldData.keySet(), newData.keySet())
                .forEach(key -> sb.append(report(key.replaceAll("`|'", ""), oldData, newData, fieldCommentMap)));
        CollectionUtil.privateKeys(oldData.keySet(), newData.keySet())
                .forEach(key -> sb.append(report(key.replaceAll("`|'", ""), oldData, newData, fieldCommentMap)));
        sb.append("\r\n}");
        return sb.toString();
    }

    private static String report(String key, Map<String, Object> oldData, Map<String, Object> newData, Map<String, String> fieldCommentMap) {
        StringBuilder sb = new StringBuilder();
        if (!BoolUtil.eq(oldData.get(key), newData.get(key))) {
            sb.append("\r\n");
            sb.append("\t");
            sb.append(String.format("%s:[%s]->[%s] ",
                    NullUtil.get(fieldCommentMap.get(key), key), oldData.get(key), newData.get(key)));
        }
        return sb.toString();
    }
}