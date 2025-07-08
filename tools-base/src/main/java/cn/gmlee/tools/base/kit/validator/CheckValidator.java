package cn.gmlee.tools.base.kit.validator;

import cn.gmlee.tools.base.anno.Check;
import cn.gmlee.tools.base.anno.El;
import cn.gmlee.tools.base.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.PropertyPlaceholderHelper;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 枚举校验器.
 *
 * @author Jas
 */
@Slf4j
public class CheckValidator implements ConstraintValidator<Check, Object> {
    private Check check;

    @Override
    public void initialize(Check check) {
        this.check = check;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Field[] fields = value.getClass().getDeclaredFields();
        Map<String, Object> valuesMap = ClassUtil.generateMap(value);
        // 支持类修饰 && 字段修饰
        return checkClass(context, valuesMap) && checkFields(context, valuesMap, fields);
    }

    private boolean eval(Map<String, Object> valuesMap, El el) {
        if (BoolUtil.isNull(el)) {
            return true; // 表达式是空不检查
        }
        String[] conditions = el.conditions();
        boolean condition = eval(valuesMap, conditions);
        if (!condition) { // 条件不满足不检查表达式
            return true;
        }
        String[] expressions = el.value();
        return eval(valuesMap, expressions);
    }

    private boolean eval(Map<String, Object> valuesMap, String... expressions) {
        if (BoolUtil.isEmpty(expressions)) {
            return true;
        }
        for (String condition : expressions) {
            // 处理空符
            CollectionUtil.valReplace(valuesMap, (k, v) -> {
                // 因为script会忽略空白符; 所以需要将空内容替换成带引号的空内容
                if (v == null) {
                    return "null";
                }
                if ("".equals(v)) {
                    return "''";
                }
                if (RegexUtil.match(v.toString(), String.format("[\\s]{%s,}", v.toString().length()))) {
                    return "\"\"";
                }
                return v.toString();
            });
            // 参数替换
            condition = PlaceholderHelper.replace(condition, valuesMap);
            // 脚本执行
            Object ok = ScriptUtil.eval(condition);
            log.info("cn.gmlee.tools.base.kit.validator.CheckValidator#execute\r\n\tcondition:{}:{}", ok, condition);
            // 执行结果
            if (ok instanceof Boolean && !(Boolean) ok) {
                return false;
            }
        }
        return true;
    }

    private boolean checkFields(ConstraintValidatorContext context, Map<String, Object> valuesMap, Field... fields) {
        for (Field field : fields) {
            El el = field.getAnnotation(El.class);
            if (!eval(valuesMap, el)) {
                context.disableDefaultConstraintViolation(); // 禁用默认的约束违规消息
                context.buildConstraintViolationWithTemplate(el.message()).addConstraintViolation();
                return false;
            }
        }
        return true;
    }

    private boolean checkClass(ConstraintValidatorContext context, Map<String, Object> valuesMap) {
        El[] els = check.value();
        for (El el : els) {
            if (!eval(valuesMap, el)) {
                context.disableDefaultConstraintViolation(); // 禁用默认的约束违规消息
                context.buildConstraintViolationWithTemplate(el.message()).addConstraintViolation();
                return false;
            }
        }
        return true;
    }


    /**
     * 依赖于Spring的帮助类,请确保依赖包存在
     */
    @SuppressWarnings("all")
    public static class PlaceholderHelper {

        private static final PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");

        /**
         * 占位符替换工具.
         *
         * @param content 内容
         * @param map     注意: 非String类型的value不会被替换
         * @return the string
         */
        public static String replace(String content, Map map) {
            if (BoolUtil.isEmpty(content)) {
                return content;
            }
            Properties properties = new Properties();
            if (BoolUtil.notEmpty(map)) {
                Set keys = map.keySet();
                for (Object key : keys) {
                    Object val = map.get(key);
                    if (val == null) {
                        map.put(key, "");
                    }
                    // 特殊处理: 支持无占位符替换字段
                    if(BoolUtil.allNotNull(key, val)){
                        content = content.replace(key.toString(), val.toString());
                    }
                }
                properties.putAll(map);
            }
            return helper.replacePlaceholders(content, properties);
        }

        /**
         * Handle string.
         *
         * @param content the content
         * @param key     the key
         * @param val     the val
         * @return the string
         */
        public static String replace(String content, String key, Object val) {
            if (BoolUtil.isEmpty(content)) {
                return content;
            }
            Properties properties = new Properties();
            properties.put(key, val != null ? val.toString() : "");
            return helper.replacePlaceholders(content, properties);
        }
    }
}
