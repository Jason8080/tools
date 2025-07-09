package cn.gmlee.tools.base.util;

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
        return get(source, target, 3);
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
        // 数据准备
        List<Diff> diffs = new ArrayList<>();
        if (BoolUtil.allNull(source, target)) {
            diffs.add(new Diff(source, target));
            return diffs;
        }
        // 数据分类
        T t = NullUtil.first(source, target);
        if (t instanceof Collection){
            diffs.addAll(getCompareList(NullUtil.get((Collection) source), NullUtil.get((Collection) target), --deep));
        }else if (t instanceof Map) {
            diffs.addAll(getRecursionMap(NullUtil.get((Map) source), NullUtil.get((Map) target), --deep));
        } else if (BoolUtil.isBean(t, Comparable.class)) {
            diffs.addAll(getRecursionMap(ClassUtil.generateMap(source), ClassUtil.generateMap(target), --deep));
        }
        return diffs;
    }

    private static Collection<Diff> getCompareList(Collection source, Collection target, int deep) {
        List<Diff> diffs = new ArrayList<>();
        if(BoolUtil.allEmpty(source, target)){
            return diffs;
        }
        // 对齐所有元素
        int size = Math.max(source.size(), target.size());
        List sourceList = new ArrayList(size);
        List targetList = new ArrayList(size);
        sourceList.addAll(source);
        targetList.addAll(target);
        for (int i = 0; i < size; i++){
            Object sv = sourceList.get(i);
            Object tv = targetList.get(i);
            Diff diff = new Diff(sv, tv);
            if (deep >= 0) {
                diff.setSubset(get(sv, tv, deep));
            }
            diffs.add(diff);
        }
        return diffs;
    }

    private static Collection<Diff> getRecursionMap(Map source, Map target, int deep) {
        List<Diff> diffs = new ArrayList<>();
        if (BoolUtil.allEmpty(source, target)) {
            return diffs;
        }
        // 获取所有字段
        Collection keys = CollectionUtil.merge(source.keySet(), target.keySet());
        for (Object key : new HashSet(keys)) {
            Object sv = source.get(key);
            Object tv = target.get(key);
            Diff diff = new Diff(key, sv, tv);
            if (deep >= 0) {
                diff.setSubset(get(sv, tv, deep));
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