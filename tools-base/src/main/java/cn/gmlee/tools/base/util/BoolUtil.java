package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.mod.R;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 通用布尔值判断工具
 *
 * @author Jas °
 * @date 2020 /9/28 (周一)
 */
public class BoolUtil {

    /**
     * 是否对象类型.
     * <p>
     * 如果String不认为是Bean, 可以在 {@param excludeClasses} 中添加String.class
     * </p>
     *
     * @param clazz          对象
     * @param excludeClasses 注意: 这里的参数是排除: 不参与判断的类型 (一旦匹配则返回false)
     * @return 空和非对象均返回false boolean
     */
    public static boolean isBean(Class clazz, Class... excludeClasses) {
        if (isNull(clazz)) {
            return false;
        }
        return !existSuperclass(clazz, excludeClasses)
                && !ClassUtils.isPrimitiveOrWrapper(clazz)
                && !ClassUtils.isPrimitiveArray(clazz)
                && !ClassUtils.isPrimitiveWrapperArray(clazz);
    }


    /**
     * Is java class boolean.
     *
     * @param obj            the obj
     * @param excludeClasses the exclude classes
     * @return the boolean
     */
    public static boolean isJavaClass(Object obj, Class... excludeClasses) {
        if (isNull(obj)) {
            return false;
        }
        if (obj instanceof Class) {
            Class clazz = (Class) obj;
            if (allSuperclass(clazz, excludeClasses)) {
                return false;
            }
            return clazz.getName().startsWith("java");
        }
        return isJavaClass(obj.getClass());
    }

    /**
     * 是子类类型.
     *
     * @param superclass 父类
     * @param subclasses 子类
     * @return boolean 全部是子类
     */
    public static boolean allSubclass(Class superclass, Class... subclasses) {
        if (isNull(superclass) || isEmpty(subclasses)) {
            return false;
        }
        for (Class subclass : subclasses) {
            if (subclass == null || !superclass.isAssignableFrom(subclass)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 存在子类.
     *
     * @param superclass the superclass
     * @param subclasses the subclasses
     * @return the boolean
     */
    public static boolean existSubclass(Class superclass, Class... subclasses) {
        if (isNull(superclass) || isEmpty(subclasses)) {
            return false;
        }
        for (Class subclass : subclasses) {
            if (subclass != null && superclass.isAssignableFrom(subclass)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 是父类类型.
     *
     * @param subclass     子类
     * @param superclasses 父类
     * @return boolean 全部是父类
     */
    public static boolean allSuperclass(Class subclass, Class... superclasses) {
        if (isNull(subclass) || isEmpty(superclasses)) {
            return false;
        }
        for (Class superclass : superclasses) {
            if (superclass == null || !superclass.isAssignableFrom(subclass)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 存在父类.
     *
     * @param subclass     the subclass
     * @param superclasses the superclasses
     * @return the boolean
     */
    public static boolean existSuperclass(Class subclass, Class... superclasses) {
        if (isNull(subclass) || isEmpty(superclasses)) {
            return false;
        }
        for (Class superclass : superclasses) {
            if (superclass != null && superclass.isAssignableFrom(subclass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断对象是否基础类.
     *
     * @param clazz          the clazz
     * @param includeClasses 注意: 这里的参数时包含
     * @return the boolean
     */
    public static boolean isBaseClass(Class clazz, Class... includeClasses) {
        if (isNull(clazz)) {
            return false;
        }
        return existSuperclass(clazz, includeClasses)
                || ClassUtils.isPrimitiveOrWrapper(clazz);
    }

    /**
     * 是否对象类型.
     * <p>
     * 如果String不认为是Bean, 可以在 {@param excludeClasses} 中添加String.class
     * </p>
     *
     * @param val            对象
     * @param excludeClasses 注意: 这里的参数是排除: 不参与判断的类型 (一旦匹配则返回false)
     * @return 空和非对象均返回false boolean
     */
    public static boolean isBean(Object val, Class... excludeClasses) {
        if (isNull(val)) {
            return false;
        }
        return isBean(val.getClass(), excludeClasses);
    }

    /**
     * 判断对象是否基础类.
     *
     * @param val            the val
     * @param includeClasses 注意: 这里的参数时包含
     * @return the boolean
     */
    public static boolean isBaseClass(Object val, Class... includeClasses) {
        if (isNull(val)) {
            return false;
        }
        return isBaseClass(val.getClass(), includeClasses);
    }

    /**
     * 判断是否是父类.
     * <p>
     * 如果两个class一致同样返回true.
     * </p>
     *
     * @param superclass the superclass
     * @param subclass   the subclass
     * @return the boolean
     */
    public static boolean isParentClass(Class superclass, Class subclass) {
        if (BoolUtil.allNotNull(superclass, subclass)) {
            return superclass.isAssignableFrom(subclass);
        }
        return false;
    }

    /**
     * 判断字符是否数字.
     *
     * @param cs the cs
     * @return the boolean
     */
    public static boolean isDigit(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = (cs.charAt(0) == '-') ? 1 : 0; i < strLen; ++i) {
                if (!Character.isDigit(cs.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 请求成功.
     *
     * @param result the result
     * @return the boolean
     */
    public static boolean isOk(R result) {
        if (result == null) {
            return false;
        }
        if (eq(XCode.OK.code, result.getCode())) {
            return true;
        }
        return between(XCode.OK_COMMAND_MIN, XCode.OK_COMMAND_MAX, result.getCode());
    }

    /**
     * 是真.
     *
     * @param o the o
     * @return the boolean
     */
    public static boolean isTrue(Boolean o) {
        if (o == null) {
            return false;
        }
        return o;
    }

    /**
     * 是假.
     *
     * @param o the o
     * @return the boolean
     */
    public static boolean isFalse(Boolean o) {
        if (o == null) {
            return true;
        }
        return !o;
    }


    /**
     * 全是真.
     *
     * @param o  the o
     * @param os the os
     * @return the boolean
     */
    public static boolean allTrue(Boolean o, Boolean... os) {
        if (!isTrue(o)) {
            return false;
        }
        for (int i = 0; i < os.length; i++) {
            if (!isTrue(os[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 全是假.
     *
     * @param o  the o
     * @param os the os
     * @return the boolean
     */
    public static boolean allFalse(Boolean o, Boolean... os) {
        if (!isFalse(o)) {
            return false;
        }
        for (int i = 0; i < os.length; i++) {
            if (!isFalse(os[i])) {
                return false;
            }
        }
        return true;
    }


    /**
     * 判断有空格
     *
     * @param cs the cs
     * @return boolean boolean
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断有空格 (任何字符串有空格就返回true)
     *
     * @param css the css
     * @return boolean boolean
     */
    public static boolean isBlank(CharSequence... css) {
        if (isNull(css)) {
            return true;
        } else {
            CharSequence[] arr$ = css;
            int len$ = css.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                CharSequence cs = arr$[i$];
                if (isBlank(cs)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * 集合是空.
     *
     * @param o the o
     * @return the boolean
     */
    public static boolean isEmpty(Collection o) {
        if (o != null && o.size() > 0) {
            return false;
        }
        return true;
    }

    /**
     * 集合是空.
     *
     * @param o the o
     * @return the boolean
     */
    public static boolean isEmpty(Map o) {
        if (o != null && o.size() > 0) {
            return false;
        }
        return true;
    }

    /**
     * 字节数组是空.
     *
     * @param bytes the bytes
     * @return the boolean
     */
    public static boolean isEmpty(byte[] bytes) {
        return bytes == null || bytes.length == 0;
    }

    /**
     * 集合是空.
     *
     * @param os the os
     * @return the boolean
     */
    public static boolean isEmpty(Collection[] os) {
        for (int i = 0; i < os.length; i++) {
            if (!isEmpty(os[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 所有集合是空.
     *
     * @param o  the o
     * @param os the os
     * @return the boolean
     */
    public static boolean allEmpty(Collection o, Collection... os) {
        if (!isEmpty(o)) {
            return false;
        }
        for (int i = 0; i < os.length; i++) {
            if (!isEmpty(os[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 所有集合是空.
     *
     * @param o  the o
     * @param os the os
     * @return the boolean
     */
    public static boolean allEmpty(Map o, Map... os) {
        if (!isEmpty(o)) {
            return false;
        }
        for (int i = 0; i < os.length; i++) {
            if (!isEmpty(os[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 所有集合是空.
     *
     * @param os  the os
     * @param oss the oss
     * @return the boolean
     */
    public static boolean allEmpty(Collection[] os, Collection[]... oss) {
        if (!isEmpty(os)) {
            return false;
        }
        for (int i = 0; i < oss.length; i++) {
            if (!isEmpty(oss[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 集合不是空.
     *
     * @param o the o
     * @return the boolean
     */
    public static boolean notEmpty(Collection o) {
        if (o == null || o.size() < 1) {
            return false;
        }
        return true;
    }

    /**
     * 集合不是空.
     *
     * @param o the o
     * @return the boolean
     */
    public static boolean notEmpty(Map o) {
        if (o == null || o.size() < 1) {
            return false;
        }
        return true;
    }

    /**
     * 集合不是空.
     *
     * @param os the os
     * @return the boolean
     */
    public static boolean notEmpty(Collection[] os) {
        for (int i = 0; i < os.length; i++) {
            if (isEmpty(os[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 所有集合不是空.
     *
     * @param o  the o
     * @param os the os
     * @return the boolean
     */
    public static boolean allNotEmpty(Collection o, Collection... os) {
        if (isEmpty(o)) {
            return false;
        }
        for (int i = 0; i < os.length; i++) {
            if (isEmpty(os[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 所有集合不是空.
     *
     * @param o  the o
     * @param os the os
     * @return the boolean
     */
    public static boolean allNotEmpty(Map o, Map... os) {
        if (isEmpty(o)) {
            return false;
        }
        for (int i = 0; i < os.length; i++) {
            if (isEmpty(os[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 所有集合不是空.
     *
     * @param os  the os
     * @param oss the oss
     * @return the boolean
     */
    public static boolean allNotEmpty(Collection[] os, Collection[]... oss) {
        if (isEmpty(os)) {
            return false;
        }
        for (int i = 0; i < oss.length; i++) {
            if (isEmpty(oss[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 数组是空.
     *
     * @param os the os
     * @return the boolean
     */
    public static boolean isEmpty(Object[] os) {
        if (os != null && os.length > 0) {
            return false;
        }
        return true;
    }

    /**
     * 所有数组是空.
     *
     * @param os  the os
     * @param oss the oss
     * @return the boolean
     */
    public static boolean allEmpty(Object[] os, Object[]... oss) {
        if (!isEmpty(os)) {
            return false;
        }
        for (int i = 0; i < oss.length; i++) {
            if (!isEmpty(oss[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 数组不是空.
     *
     * @param os the os
     * @return the boolean
     */
    public static boolean notEmpty(Object[] os) {
        if (os == null || os.length < 1) {
            return false;
        }
        return true;
    }

    /**
     * 所有数组不是空.
     *
     * @param os  the os
     * @param oss the oss
     * @return the boolean
     */
    public static boolean allNotEmpty(Object[] os, Object[]... oss) {
        if (isEmpty(os)) {
            return false;
        }
        for (int i = 0; i < oss.length; i++) {
            if (isEmpty(oss[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否为空.
     *
     * @param o the o
     * @return 是空或长度小于1则返回true boolean
     */
    public static boolean isEmpty(CharSequence o) {
        if (o != null && o.length() > 0) {
            return false;
        }
        return true;
    }

    /**
     * 判断字符串是否全部为空.
     *
     * @param o  the o
     * @param os the os
     * @return 全部是空则返回true boolean
     */
    public static boolean allEmpty(CharSequence o, CharSequence... os) {
        if (!isEmpty(o)) {
            return false;
        }
        for (int i = 0; i < os.length; i++) {
            if (!isEmpty(os[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否不为空.
     *
     * @param <O> the type parameter
     * @param o   the o
     * @return 不是空并且长度大于0则返回true boolean
     */
    public static <O extends CharSequence> boolean notEmpty(O o) {
        if (o == null || o.length() < 1) {
            return false;
        }
        return true;
    }

    /**
     * 判断字符串是否全部不为空.
     *
     * @param o  the o
     * @param os the os
     * @return 全部不是空则返回true boolean
     */
    public static boolean allNotEmpty(CharSequence o, CharSequence... os) {
        if (isEmpty(o)) {
            return false;
        }
        for (int i = 0; i < os.length; i++) {
            if (isEmpty(os[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断对象属性值是否均为空
     *
     * @param o the o
     * @return boolean boolean
     */
    public static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        }
        Map<String, Object> map = ClassUtil.generateMap(o, true);
        return map.isEmpty();
    }

    /**
     * 判断对象是否为空.
     *
     * @param o the o
     * @return the boolean
     */
    public static boolean isNull(Object o) {
        if (o != null) {
            return false;
        }
        return true;
    }

    /**
     * 判断对象是否全部为空.
     *
     * @param o  the o
     * @param os the os
     * @return 全部是空则返回true boolean
     */
    public static boolean allNull(Object o, Object... os) {
        if (!isNull(o)) {
            return false;
        }
        for (int i = 0; i < os.length; i++) {
            if (!isNull(os[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断对象是否不为空.
     *
     * @param o the o
     * @return 对象不是空则返回true boolean
     */
    public static boolean notNull(Object o) {
        if (o == null) {
            return false;
        }
        return true;
    }

    /**
     * 判断对象是否全部不为空.
     *
     * @param o  the o
     * @param os the os
     * @return 全部不是空则返回true boolean
     */
    public static boolean allNotNull(Object o, Object... os) {
        if (isNull(o)) {
            return false;
        }
        for (int i = 0; i < os.length; i++) {
            if (isNull(os[i])) {
                return false;
            }
        }
        return true;
    }


    /**
     * In boolean.
     *
     * @param target  the target
     * @param sources the sources
     * @return the boolean
     */
    public static boolean in(Object target, Object... sources) {
        if (isEmpty(sources)) {
            return false;
        }
        for (Object source : sources) {
            if (Objects.equals(target, source)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在指定数值之间.
     *
     * @param min    the min
     * @param max    the max
     * @param target the target
     * @return the boolean
     */
    public static boolean between(Comparable min, Comparable max, Comparable target) {
        return gte(target, min) && lte(target, max);
    }

    /**
     * 判断source是否等于target.
     *
     * @param source the source
     * @param target the target
     * @return the boolean
     */
    public static boolean eq(Object source, Object target) {
        return Objects.equals(source, target);
    }

    /**
     * 判断数值是否相等.
     *
     * @param source the source
     * @param target the target
     * @return the boolean
     */
    public static boolean eq(Comparable source, Comparable target) {
        if (allNotNull(source, target)) {
            return source.compareTo(target) == Int.ZERO;
        }
        return false;
    }

    /**
     * 判断source是否大于target.
     *
     * @param source the source
     * @param target the target
     * @return the boolean
     */
    public static boolean gt(Comparable source, Comparable target) {
        if (allNotNull(source, target)) {
            int i = source.compareTo(target);
            return i > 0;
        }
        return false;
    }


    /**
     * 全部大于目标值.
     *
     * @param target  the target
     * @param sources the sources
     * @return the boolean
     */
    public static boolean allGt(Comparable target, Comparable... sources) {
        for (Comparable source : sources) {
            boolean gt = gt(source, target);
            if (!gt) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断source是否大于等于target.
     *
     * @param source the source
     * @param target the target
     * @return the boolean
     */
    public static boolean gte(Comparable source, Comparable target) {
        if (allNotNull(source, target)) {
            int i = source.compareTo(target);
            return i >= 0;
        }
        return false;
    }

    /**
     * 全部大于等于目标值.
     *
     * @param target  the target
     * @param sources the sources
     * @return the boolean
     */
    public static boolean allGte(Comparable target, Comparable... sources) {
        for (Comparable source : sources) {
            boolean gt = gte(source, target);
            if (!gt) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断source是否小于target.
     *
     * @param source the source
     * @param target the target
     * @return the boolean
     */
    public static boolean lt(Comparable source, Comparable target) {
        if (allNotNull(source, target)) {
            int i = source.compareTo(target);
            return i < 0;
        }
        return false;
    }

    /**
     * 全部小于目标值.
     *
     * @param target  the target
     * @param sources the sources
     * @return the boolean
     */
    public static boolean allLt(Comparable target, Comparable... sources) {
        for (Comparable source : sources) {
            boolean gt = lt(source, target);
            if (!gt) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断source是否小于等于target.
     *
     * @param source the source
     * @param target the target
     * @return the boolean
     */
    public static boolean lte(Comparable source, Comparable target) {
        if (allNotNull(source, target)) {
            int i = source.compareTo(target);
            return i <= 0;
        }
        return false;
    }

    /**
     * 全部小于等于目标值.
     *
     * @param target  the target
     * @param sources the sources
     * @return the boolean
     */
    public static boolean allLte(Comparable target, Comparable... sources) {
        for (Comparable source : sources) {
            boolean gt = lte(source, target);
            if (!gt) {
                return false;
            }
        }
        return true;
    }


    /**
     * 判断source是否包含target.
     * <p>
     * 任何值为空返回false
     * </p>
     *
     * @param source the source
     * @param target the target
     * @return the boolean
     */
    public static boolean contain(String source, String target) {
        if (isEmpty(source) || isEmpty(target)) {
            return false;
        }
        return source.contains(target);
    }

    /**
     * Not contain boolean.
     *
     * @param source the source
     * @param target the target
     * @return the boolean
     */
    public static boolean notContain(String source, String target) {
        return !contain(source, target);
    }

    /**
     * 匹配一个.
     * <p>
     * 匹配与包含的区别在于:
     * 包含: 集合判断包含的时候, 是全字符串对比
     * 匹配: 集合判断包含的时候, 是每个对比是否包含(String.contain(..))
     * </p>
     *
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static boolean matchingOne(Collection<String> source, String... targets) {
        if (isEmpty(source) || isEmpty(targets)) {
            return false;
        }
        for (int i = 0; i < targets.length; i++) {
            for (String s : source) {
                if (BoolUtil.contain(s, targets[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Matching one boolean.
     *
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static boolean matchingOne(String[] source, String... targets) {
        return matchingOne(Arrays.asList(source), targets);
    }

    /**
     * 匹配所有.
     *
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static boolean matchingAll(Collection<String> source, String... targets) {
        if (isEmpty(source) || isEmpty(targets)) {
            return false;
        }
        for (int i = 0; i < targets.length; i++) {
            for (String s : source) {
                if (!BoolUtil.contain(s, targets[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Matching all boolean.
     *
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static boolean matchingAll(String[] source, String... targets) {
        return matchingAll(Arrays.asList(source), targets);
    }

    /**
     * Not matching one boolean.
     *
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static boolean notMatchingOne(Collection<String> source, String... targets) {
        return !matchingOne(source, targets);
    }

    /**
     * Not matching one boolean.
     *
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static boolean notMatchingOne(String[] source, String... targets) {
        return !matchingOne(source, targets);
    }

    /**
     * Not matching all boolean.
     *
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static boolean notMatchingAll(Collection<String> source, String... targets) {
        return !matchingAll(source, targets);
    }

    /**
     * Not matching all boolean.
     *
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static boolean notMatchingAll(String[] source, String... targets) {
        return !matchingAll(source, targets);
    }

    /**
     * 判断source是否包含所有target.
     * <p>
     * 任何值为空返回false
     * </p>
     *
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static boolean containAll(String source, String... targets) {
        if (isNull(source) || isEmpty(targets)) {
            return false;
        }
        for (int i = 0; i < targets.length; i++) {
            boolean contain = contain(source, targets[i]);
            if (!contain) {
                return false;
            }
        }
        return true;
    }

    /**
     * Not contain all boolean.
     *
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static boolean notContainAll(String source, String... targets) {
        return !containAll(source, targets);
    }

    /**
     * 判断source是否包含target.
     * <p>
     * 任何值为空返回false
     * </p>
     *
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static boolean containOne(String source, String... targets) {
        if (isNull(source) || isEmpty(targets)) {
            return false;
        }
        for (int i = 0; i < targets.length; i++) {
            boolean contain = contain(source, targets[i]);
            if (contain) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not contain one boolean.
     *
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static boolean notContainOne(String source, String... targets) {
        return !containOne(source, targets);
    }

    /**
     * 判断source是否包含所有target.
     * <p>
     * 任何值为空返回false
     * </p>
     *
     * @param <T>     the type parameter
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static <T> boolean containAll(T[] source, T... targets) {
        if (isEmpty(source) || isEmpty(targets)) {
            return false;
        }
        for (int i = 0; i < source.length; i++) {
            T st = source[i];
            for (int j = 0; j < targets.length; j++) {
                if (!Objects.equals(st, targets[j])) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Not contain all boolean.
     *
     * @param <T>     the type parameter
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static <T> boolean notContainAll(T[] source, T... targets) {
        return !containAll(source, targets);
    }

    /**
     * 判断source是否包含target.
     * <p>
     * 任何值为空返回false
     * </p>
     *
     * @param <T>     the type parameter
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static <T> boolean containOne(T[] source, T... targets) {
        if (isEmpty(source) || isEmpty(targets)) {
            return false;
        }
        for (int i = 0; i < source.length; i++) {
            T st = source[i];
            for (int j = 0; j < targets.length; j++) {
                if (Objects.equals(st, targets[j])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Contain one boolean.
     *
     * @param <T>     the type parameter
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static <T> boolean containOne(T source, T... targets) {
        if (isNull(source) || isEmpty(targets)) {
            return false;
        }
        for (int i = 0; i < targets.length; i++) {
            T target = targets[i];
            if (Objects.equals(source, target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not contain one boolean.
     *
     * @param <T>     the type parameter
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static <T> boolean notContainOne(T[] source, T... targets) {
        return !containOne(source, targets);
    }

    /**
     * Not contain one boolean.
     *
     * @param <T>     the type parameter
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static <T> boolean notContainOne(T source, T... targets) {
        return !containOne(source, targets);
    }

    /**
     * 判断source是否包含所有target.
     * <p>
     * 任何值为空返回false
     * </p>
     *
     * @param <T>     the type parameter
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static <T> boolean containAll(Collection<T> source, T... targets) {
        if (isEmpty(source) || isEmpty(targets)) {
            return false;
        }
        for (int i = 0; i < targets.length; i++) {
            if (!source.contains(targets[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Not contain all boolean.
     *
     * @param <T>     the type parameter
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static <T> boolean notContainAll(Collection<T> source, T... targets) {
        return !containAll(source, targets);
    }

    /**
     * 判断source是否包含target.
     * <p>
     * 任何值为空返回false
     * </p>
     *
     * @param <T>     the type parameter
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static <T> boolean containOne(Collection<T> source, T... targets) {
        if (isEmpty(source) || isEmpty(targets)) {
            return false;
        }
        for (int i = 0; i < targets.length; i++) {
            if (source.contains(targets[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not contain one boolean.
     *
     * @param <T>     the type parameter
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static <T> boolean notContainOne(Collection<T> source, T... targets) {
        return !containOne(source, targets);
    }

    /**
     * 判断map是否包含所有key.
     * <p>
     * 任何值为空返回false
     * </p>
     *
     * @param <K>  the type parameter
     * @param <V>  the type parameter
     * @param map  the map
     * @param keys the keys
     * @return the boolean
     */
    public static <K, V> boolean containKeys(Map<K, V> map, K... keys) {
        if (isEmpty(map) || isEmpty(keys)) {
            return false;
        }
        for (int i = 0; i < keys.length; i++) {
            if (!map.containsKey(keys[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Not contain keys boolean.
     *
     * @param <K>  the type parameter
     * @param <V>  the type parameter
     * @param map  the map
     * @param keys the keys
     * @return the boolean
     */
    public static <K, V> boolean notContainKeys(Map<K, V> map, K... keys) {
        return !containKeys(map, keys);
    }

    /**
     * 判断map是否包含key.
     * <p>
     * 任何值为空返回false
     * </p>
     *
     * @param <K>  the type parameter
     * @param <V>  the type parameter
     * @param map  the map
     * @param keys the keys
     * @return the boolean
     */
    public static <K, V> boolean containKey(Map<K, V> map, K... keys) {
        if (isEmpty(map) || isEmpty(keys)) {
            return false;
        }
        for (int i = 0; i < keys.length; i++) {
            if (map.containsKey(keys[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not contain key boolean.
     *
     * @param <K>  the type parameter
     * @param <V>  the type parameter
     * @param map  the map
     * @param keys the keys
     * @return the boolean
     */
    public static <K, V> boolean notContainKey(Map<K, V> map, K... keys) {
        return !containKey(map, keys);
    }

    /**
     * 判断map是否包含所有val.
     * <p>
     * 任何值为空返回false
     * </p>
     *
     * @param <K>    the type parameter
     * @param <V>    the type parameter
     * @param map    the map
     * @param values the values
     * @return the boolean
     */
    public static <K, V> boolean containValues(Map<K, V> map, V... values) {
        if (isEmpty(map) || isEmpty(values)) {
            return false;
        }
        for (int i = 0; i < values.length; i++) {
            if (!map.containsValue(values[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Not contain values boolean.
     *
     * @param <K>    the type parameter
     * @param <V>    the type parameter
     * @param map    the map
     * @param values the values
     * @return the boolean
     */
    public static <K, V> boolean notContainValues(Map<K, V> map, V... values) {
        return !containValues(map, values);
    }

    /**
     * 判断map是否包含val.
     * <p>
     * 任何值为空返回false
     * </p>
     *
     * @param <K>    the type parameter
     * @param <V>    the type parameter
     * @param map    the map
     * @param values the values
     * @return the boolean
     */
    public static <K, V> boolean containValue(Map<K, V> map, V... values) {
        if (isEmpty(map) || isEmpty(values)) {
            return false;
        }
        for (int i = 0; i < values.length; i++) {
            if (map.containsValue(values[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not contain value boolean.
     *
     * @param <K>    the type parameter
     * @param <V>    the type parameter
     * @param map    the map
     * @param values the values
     * @return the boolean
     */
    public static <K, V> boolean notContainValue(Map<K, V> map, V... values) {
        return !containValue(map, values);
    }
}
