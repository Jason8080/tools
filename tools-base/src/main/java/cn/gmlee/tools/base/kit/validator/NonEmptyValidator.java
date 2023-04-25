package cn.gmlee.tools.base.kit.validator;

import cn.gmlee.tools.base.anno.NonEmpty;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ClassUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.Map;

/**
 * 必填校验器.
 *
 * @author Jas
 */
public class NonEmptyValidator implements ConstraintValidator<NonEmpty, Object> {
    private NonEmpty nonEmpty;

    @Override
    public void initialize(NonEmpty nonEmpty) {
        this.nonEmpty = nonEmpty;
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        NonEmpty.Group[] groups = nonEmpty.value();
        if (BoolUtil.isEmpty(groups)) {
            return true;
        }
        for (NonEmpty.Group group : groups) {
            if (!checkGroup(obj, group)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkGroup(Object obj, NonEmpty.Group group) {
        Map<String, Object> generateMap = ClassUtil.generateMap(obj);
        // 不存在才检测
        if (!checkNonExist(group, generateMap)) {
            return true;
        }
        // 不存在不检测
        if (!checkExist(group, generateMap)) {
            return true;
        }
        if (checkOnes(group, generateMap)) {
            return true;
        }
        return false;
    }

    private boolean checkOnes(NonEmpty.Group group, Map<String, Object> generateMap) {
        NonEmpty.Field[] fields = group.value();
        if (BoolUtil.isEmpty(fields)) {
            return true;
        }
        for (NonEmpty.Field field : fields) {
            if (checkOne(generateMap, field)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNonExist(NonEmpty.Group group, Map<String, Object> generateMap) {
        String[] exist = group.nonExist();
        if (BoolUtil.notEmpty(exist)) {
            for (String name : exist) {
                if (exist(generateMap, name)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkExist(NonEmpty.Group group, Map<String, Object> generateMap) {
        String[] exist = group.exist();
        if (BoolUtil.notEmpty(exist)) {
            for (String name : exist) {
                if (!exist(generateMap, name)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkOne(Map<String, Object> generateMap, NonEmpty.Field field) {
        boolean and = field.and();
        String[] value = field.value();
        if (BoolUtil.isEmpty(value)) {
            return true;
        }
        if (and) {
            for (String name : value) {
                if (!exist(generateMap, name)) {
                    return false;
                }
            }
            return true;
        } else {
            for (String name : value) {
                if (exist(generateMap, name)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean exist(Map<String, Object> generateMap, String name) {
        Object val = generateMap.get(name);
        if (val instanceof Object[] && BoolUtil.notEmpty((Object[]) val)) {
            return true;
        }
        if (val instanceof Collection && BoolUtil.notEmpty((Collection) val)) {
            return true;
        }
        if (val instanceof Map && BoolUtil.notEmpty((Map) val)) {
            return true;
        }
        if (val instanceof String && BoolUtil.notEmpty((String) val)) {
            return true;
        }
        if (!(val instanceof Object[]) && !(val instanceof Collection) && !(val instanceof Map) && !(val instanceof String) && val != null) {
            return true;
        }
        return false;
    }
}
