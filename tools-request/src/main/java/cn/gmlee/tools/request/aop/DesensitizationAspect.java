package cn.gmlee.tools.request.aop;

import cn.gmlee.tools.base.util.*;
import cn.gmlee.tools.request.mod.Desensitization;
import cn.gmlee.tools.request.config.DesensitizationProperties;
import cn.gmlee.tools.request.config.ParameterProperties;
import cn.gmlee.tools.request.config.EncryptProperties;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpEntity;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.AntPathMatcher;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Jas°
 * @date 2021/5/17 (周一)
 */
@Aspect
public class DesensitizationAspect {

    private EncryptProperties encryptProperties;
    private DesensitizationProperties desensitizationProperties;
    private List<Desensitization> desensitizationList;
    private List<String> desensitizationUrlExcludes;
    private List<Class<?>> classExcludes;

    private AntPathMatcher matcher = new AntPathMatcher();

    public DesensitizationAspect(ParameterProperties parameterProperties, EncryptProperties encryptProperties, DesensitizationProperties desensitizationProperties) {
        this.encryptProperties = encryptProperties;
        this.desensitizationProperties = desensitizationProperties;
        this.desensitizationList = parameterProperties.getDesensitization();
        this.desensitizationUrlExcludes = parameterProperties.getDesensitizationUrlExcludes();
        this.classExcludes = parameterProperties.getClassExcludes();
        classExcludes.add(BeanPropertyBindingResult.class);
    }

    @Pointcut("execution(public !void *..*Controller.*(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint point) {
        if (isUrlExcludes() || !encryptProperties.getEnable()) {
            return;
        }
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        // 请求接口
        Class apiClass = methodSignature.getDeclaringType();
        // 请求方法
        Method methodObj = methodSignature.getMethod();
        // 请求参数
        Object[] args = point.getArgs();
        for (Object arg : args) {
            encryptHandler(arg);
        }
    }

    @AfterReturning(value = "pointcut()", returning = "result")
    public void afterReturning(JoinPoint point, Object result) {
        if (isUrlExcludes() || !desensitizationProperties.getEnable()) {
            return;
        }
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        // 请求接口
        Class apiClass = methodSignature.getDeclaringType();
        // 请求方法
        Method methodObj = methodSignature.getMethod();
        // 请求参数
        Object[] args = point.getArgs();
        for (Object arg : args) {
            if (arg instanceof Model) {
                desensitizationHandler(arg);
            } else if (arg instanceof ModelMap) {
                desensitizationHandler(arg);
            }
        }
        if (result instanceof HttpEntity) {

        } else if (result instanceof String) {

        } else if (result instanceof ModelAndView) {
            desensitizationHandler(result);
        } else {
            desensitizationHandler(result);
        }
    }

    private boolean isUrlExcludes() {
        String relativePath = WebUtil.getCurrentServletPath();
        if (BoolUtil.notEmpty(relativePath)) {
            for (String pattern : desensitizationUrlExcludes) {
                boolean match = matcher.match(pattern, relativePath);
                if (match) {
                    return true;
                }
            }
        }
        return false;
    }

    private void encryptHandler(Object obj) {
        if (obj instanceof Model) {

        } else if (obj instanceof ModelMap) {

        } else if (obj instanceof File) {

        } else if (obj instanceof MultipartFile) {

        } else {
            encryptFor(obj);
        }
    }

    private void desensitizationHandler(Object obj) {
        if (obj instanceof Model) {
            desensitizationFor(((Model) obj).asMap());
        } else if (obj instanceof ModelMap) {
            desensitizationFor(obj);
        } else {
            desensitizationFor(obj);
        }
    }

    private void encryptFor(Object obj) {
        if (obj instanceof Map) {
            encryptForMap((Map) obj);
        } else {
            encryptForBean(obj);
        }
    }

    private void desensitizationFor(Object obj) {
        if (obj instanceof Map) {
            desensitizationForMap((Map) obj);
        } else {
            desensitizationForBean(obj);
        }
    }

    private String desensitizationForString(Object key, String val) {
        for (Desensitization desensitization : desensitizationList) {
            if (desensitization.getUse() && RegexUtil.matchMoreRegex(val, desensitization.getRule())) {
                return DesensitizationUtil.hide(val);
            } else if (!desensitization.getUse() && BoolUtil.eq(key, desensitization.getRule())) {
                return DesensitizationUtil.hide(val);
            }
        }
        return val;
    }

    private Collection encryptForList(String key, Collection collection) {
        if (BoolUtil.notEmpty(collection) && BoolUtil.notEmpty(desensitizationList)) {
            Iterator it = collection.iterator();
            while (it.hasNext()) {
                Object val = it.next();
                // 只对字符串脱敏
                if (val == null) {
                    continue;
                } else if (val instanceof Collection) {
                    return encryptForList(key, (List) val);
                } else if (val instanceof Map) {
                    encryptForMap((Map) val);
                } else if (BoolUtil.isBean(val, classExcludes.toArray(new Class[0]))) {
                    encryptForBean(val);
                } else {
                    it.remove();
                    collection.add(SnUtil.encode(val.toString(), encryptProperties.getCipher()));
                }
            }
        }
        return collection;
    }

    private Collection desensitizationForList(String key, Collection collection) {
        if (BoolUtil.notEmpty(collection) && BoolUtil.notEmpty(desensitizationList)) {
            Iterator it = collection.iterator();
            while (it.hasNext()) {
                Object val = it.next();
                // 只对字符串脱敏
                if (val == null) {
                    continue;
                } else if (val instanceof String) {
                    it.remove();
                    collection.add(desensitizationForString(key, (String) val));
                } else if (val instanceof Collection) {
                    return desensitizationForList(key, (List) val);
                } else if (val instanceof Map) {
                    desensitizationForMap((Map) val);
                } else if (BoolUtil.isBean(val, classExcludes.toArray(new Class[0]))) {
                    desensitizationForBean(val);
                }
            }
        }
        return collection;
    }

    private Map encryptForMap(Map<String, Object> map) {
        if (BoolUtil.notEmpty(map) && BoolUtil.notEmpty(desensitizationList)) {
            Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> next = it.next();
                String key = next.getKey();
                Object val = next.getValue();
                // 只对字符串脱敏
                if (val == null) {
                    continue;
                } else if (val instanceof Collection) {
                    Collection cs = encryptForList(key, (List) val);
                    next.setValue(cs);
                } else if (val instanceof Map) {
                    encryptForMap((Map) val);
                } else if (BoolUtil.isBean(val, classExcludes.toArray(new Class[0]))) {
                    encryptForBean(val);
                } else {
                    String desVal = SnUtil.encode(val.toString(), encryptProperties.getCipher());
                    next.setValue(desVal);
                }
            }
        }
        return map;
    }

    private Map desensitizationForMap(Map<String, Object> map) {
        if (BoolUtil.notEmpty(map) && BoolUtil.notEmpty(desensitizationList)) {
            Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> next = it.next();
                String key = next.getKey();
                Object val = next.getValue();
                // 只对字符串脱敏
                if (val == null) {
                    continue;
                } else if (val instanceof String) {
                    String desVal = desensitizationForString(key, (String) val);
                    next.setValue(desVal);
                } else if (val instanceof Collection) {
                    Collection cs = desensitizationForList(key, (List) val);
                    next.setValue(cs);
                } else if (val instanceof Map) {
                    desensitizationForMap((Map) val);
                } else if (BoolUtil.isBean(val, classExcludes.toArray(new Class[0]))) {
                    desensitizationForBean(val);
                }
            }
        }
        return map;
    }

    private void encryptForBean(Object obj) {
        Map<String, Field> fieldsMap = ClassUtil.getFieldsMap(obj);
        Iterator<Map.Entry<String, Field>> iterator = fieldsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Field> entry = iterator.next();
            String name = entry.getKey();
            Field field = entry.getValue();
            if (!Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                try {
                    Object val = field.get(obj);
                    if (val == null) {
                        continue;
                    } else if (val instanceof Collection) {
                        Collection cs = encryptForList(name, (Collection) val);
                        field.set(obj, cs);
                    } else if (val instanceof Map) {
                        encryptForMap((Map) val);
                    } else if (BoolUtil.isBean(val, classExcludes.toArray(new Class[0]))) {
                        encryptForBean(val);
                    } else {
                        String desVal = SnUtil.encode(val.toString(), encryptProperties.getCipher());
                        field.set(obj, desVal);
                    }
                } catch (Exception e) {
                } finally {
                    field.setAccessible(false);
                }
            }
        }
    }

    private void desensitizationForBean(Object obj) {
        Map<String, Field> fieldsMap = ClassUtil.getFieldsMap(obj);
        Iterator<Map.Entry<String, Field>> iterator = fieldsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Field> entry = iterator.next();
            String name = entry.getKey();
            Field field = entry.getValue();
            if (!Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                try {
                    Object val = field.get(obj);
                    if (val == null) {
                        continue;
                    } else if (val instanceof String) {
                        String desVal = desensitizationForString(name, (String) val);
                        field.set(obj, desVal);
                    } else if (val instanceof Collection) {
                        Collection cs = desensitizationForList(name, (Collection) val);
                        field.set(obj, cs);
                    } else if (val instanceof Map) {
                        desensitizationForMap((Map) val);
                    } else if (BoolUtil.isBean(val, classExcludes.toArray(new Class[0]))) {
                        desensitizationForBean(val);
                    }
                } catch (Exception e) {
                } finally {
                    field.setAccessible(false);
                }
            }
        }
    }
}
