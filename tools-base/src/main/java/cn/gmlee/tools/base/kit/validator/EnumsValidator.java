package cn.gmlee.tools.base.kit.validator;

import cn.gmlee.tools.base.anno.Enums;
import cn.gmlee.tools.base.enums.Regex;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.EnumUtil;
import cn.gmlee.tools.base.util.RegexUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
            if (enums.value().startsWith(Regex.first) && enums.value().endsWith(Regex.last)) {
                return RegexUtil.match(value.toString(), enums.value());
            }
            if (BoolUtil.containOne(enums.value().split(Enums.separator), value.toString())) {
                return true;
            }
            for (Class<? extends Enum> target : enums.enums()) {
                // 优先使用名称: 因为如果采用NAME交互, 必须与名称相同才能赋值
                if (EnumUtil.name(value, target) != null ||
                        // 其次使用内容: 在非枚举对象接收参数时, 仍然可以使用该校验
                        EnumUtil.value(value, target) != null
                ) {
                    return true;
                }
            }
            return false;
        }
        return !enums.required();
    }
}
