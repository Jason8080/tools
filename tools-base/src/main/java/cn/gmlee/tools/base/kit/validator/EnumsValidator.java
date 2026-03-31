package cn.gmlee.tools.base.kit.validator;

import cn.gmlee.tools.base.anno.Enums;
import cn.gmlee.tools.base.enums.Regex;
import cn.gmlee.tools.base.util.*;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 枚举校验器.
 *
 * @author Jas
 */
public class EnumsValidator implements ConstraintValidator<Enums, Object> {
    private Enums enums;

    @Override
    public void initialize(Enums enums) {
        this.enums = enums;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value != null) {
            // 如果已经匹配: 仅检测类型即可
            if (BoolUtil.containOne(enums.enums(), value.getClass())) {
                return true;
            }
            if (enums.value().startsWith(Regex.first) && enums.value().endsWith(Regex.last)) {
                return RegexUtil.match(value.toString(), enums.value());
            }
            if (BoolUtil.containOne(enums.value().split(Enums.separator), value.toString())) {
                return true;
            }
            for (Class<?> target : enums.enums()) {
                if (target == null) {
                    continue;
                }
                if (valueOf(value, target)) return true;
                if (hasEnum(value, target)) return true;
            }
            return false;
        }
        return !enums.required();
    }

    private static boolean valueOf(Object value, Class target) {
        Method method = ExceptionUtil.sandbox(() -> target.getMethod("valueOf", value.getClass()));
        if (method == null) {
            return false;
        }
        if (Modifier.isStatic(method.getModifiers())) {
            Object obj = ExceptionUtil.sandbox(() -> ClassUtil.call(null, method, value));
            return obj != null;
        }
        return false;
    }

    private static boolean hasEnum(Object value, Class target) {
        if (!target.isEnum()) {
            return false;
        }
        // 优先使用名称: 因为如果采用NAME交互, 必须与名称相同才能赋值
        if (EnumUtil.name(value, target) != null ||
                // 其次使用内容: 在非枚举对象接收参数时, 仍然可以使用该校验
                EnumUtil.value(value, target) != null
        ) {
            return true;
        }
        return false;
    }
}
