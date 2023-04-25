package cn.gmlee.tools.base.builder;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * 字节码构建器.
 *
 * @param <T> the type parameter
 */
public class BytecodeBuilder<T> {
    private static final Logger log = LoggerFactory.getLogger(BytecodeBuilder.class);
    /**
     * 字节码池.
     */
    public static final ClassPool classPool = ClassPool.getDefault();
    /**
     * 增强字节码加载器.
     */
    private static final Loader loader = new Loader(classPool);

    /**
     * 当前字节码增强对象.
     */
    private CtClass ctClass;

    /**
     * Instantiates a new Bytecode builder.
     *
     * @param ctClass the ct class
     */
    public BytecodeBuilder(CtClass ctClass) {
        this.ctClass = ctClass;
    }


    /**
     * 获取字节码的增强字节码对象.
     *
     * @param clazz the clazz
     * @return ct class
     */
    public static CtClass get(Class<?> clazz) {
        CtClass ctClass = classPool.getOrNull(clazz.getName());
        if (ctClass != null) {
            return ctClass;
        }
        ClassClassPath classPath = new ClassClassPath(clazz);
        classPool.appendClassPath(classPath);
        try {
            return classPool.get(clazz.getName());
        } catch (NotFoundException e) {
            log.error("字节码不存在", e);
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * 找到属于增强框架的对象.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return the ct class
     */
    public static <T> BytecodeBuilder<T> of(Class<T> clazz) {
        CtClass old = get(clazz);
        String classname = getClassname(old.getName());
        CtClass ctClass = classPool.getOrNull(classname);
        if (ctClass != null) {
            return new BytecodeBuilder(ctClass);
        }
        return new BytecodeBuilder(classPool.makeClass(classname, old));
    }


    /**
     * 检测是否已经增强.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return the boolean
     */
    public static <T> boolean exist(Class<T> clazz) {
        String classname = getClassname(clazz.getName());
        CtClass ctClass = classPool.getOrNull(classname);
        return ctClass != null;
    }

    /**
     * 添加字段.
     *
     * @param modifier  the modifier
     * @param name      字段名称
     * @param typeClass 字段类型
     * @return bytecode builder 构建器
     */
    public BytecodeBuilder<T> addField(int modifier, String name, Class typeClass) {
        try {
            CtClass typeClazz = classPool.get(typeClass.getName());
            CtField ctField = new CtField(typeClazz, name, this.ctClass);
            ctField.setModifiers(modifier);
            ctClass.addField(ctField);
        } catch (Exception e) {
            log.error("字节码增强构建字段出错", e);
            return ExceptionUtil.cast(e);
        }
        return this;
    }

    /**
     * 添加字段.
     *
     * @param name      字段名称
     * @param typeClass 字段类型
     * @return bytecode builder 构建器
     */
    public BytecodeBuilder<T> addField(String name, Class typeClass) {
        return addField(Modifier.PRIVATE, name, typeClass);
    }


    /**
     * 添加方法.
     *
     * @param modifier       修饰符 {@link Modifier}
     * @param name           方法名
     * @param retClass       返回类型
     * @param body           方法体
     * @param parameterClass 参数类型集合
     * @return bytecode builder 构建器
     */
    public BytecodeBuilder<T> addMethod(int modifier, String name, Class retClass, String body, Class... parameterClass) {
        try {
            CtClass retClazz = (retClass == null || retClass == Void.class) ?
                    CtClass.voidType : classPool.get(retClass.getName());
            CtMethod ctMethod = new CtMethod(retClazz, name, toCtClasses(parameterClass), ctClass);
            ctMethod.setModifiers(modifier);
            ctMethod.setBody(body);
            ctClass.addMethod(ctMethod);
        } catch (Exception e) {
            log.error("字节码增强构建方法出错", e);
            return ExceptionUtil.cast(e);
        }
        return this;
    }

    /**
     * 添加方法.
     *
     * @param name           方法名
     * @param retClass       返回类型
     * @param body           方法体
     * @param parameterClass 参数类型集合
     * @return bytecode builder 构建器
     */
    public BytecodeBuilder<T> addMethod(String name, Class retClass, String body, Class... parameterClass) {
        return addMethod(Modifier.PUBLIC, name, retClass, body, parameterClass);
    }


    /**
     * 加载增强字节码.
     *
     * @return the class
     */
    public Class<T> build() {
        try {
            return (Class<T>) ctClass.toClass();
        } catch (Exception e) {
            log.error("增强字节码丢失", e);
            return ExceptionUtil.cast(e);
        }
    }


    /**
     * 重载增强字节码.
     *
     * @return the class
     */
    public Class<T> rebuild() {
        try {
            return (Class<T>) loader.loadClass(ctClass.getName());
        } catch (Exception e) {
            log.error("增强字节码丢失", e);
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * 创建增强字节码对象.
     *
     * @return the t
     */
    public T buildObject() {
        try {
            Constructor<T> constructor = build().getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            log.error("反射增强字节码出错", e);
            return ExceptionUtil.cast(e);
        }
    }

    private CtClass[] toCtClasses(Class... parameterClass) {
        if (BoolUtil.isEmpty(parameterClass)) {
            return new CtClass[]{};
        }
        CtClass[] classes = new CtClass[parameterClass.length];
        for (int i = 0; i < classes.length; i++) {
            try {
                classes[i] = classPool.get(parameterClass[i].getName());
            } catch (NotFoundException e) {
                log.error("字节码丢失", e);
            }
        }
        return classes;
    }

    private static String getClassname(String old) {
        return old + "$TOOLS";
    }
}
