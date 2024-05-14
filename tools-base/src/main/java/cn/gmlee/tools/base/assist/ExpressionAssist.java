package cn.gmlee.tools.base.assist;

import cn.gmlee.tools.base.util.LocalDateTimeUtil;
import net.sf.jsqlparser.expression.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SQL字段类型辅助工具.
 */
public class ExpressionAssist {
    /**
     * As list.
     *
     * @param cs the cs
     * @return the list
     */
    public static List<Expression> as(Collection<?> cs) {
        if (cs == null) {
            return Collections.emptyList();
        }
        return cs.stream().filter(Objects::nonNull).map(ExpressionAssist::convert).collect(Collectors.toList());
    }

    /**
     * As list.
     *
     * @param cs the cs
     * @return the list
     */
    public static List<Expression> as(Object... cs) {
        if (cs == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(cs).filter(Objects::nonNull).map(ExpressionAssist::convert).collect(Collectors.toList());
    }

    private static Expression convert(Object c) {
        if (c instanceof Short || c instanceof Byte || c instanceof Integer || c instanceof Long) {
            return new LongValue(c.toString());
        } else if (c instanceof Float || c instanceof Double) {
            return new DoubleValue(c.toString());
        } else if (c instanceof CharSequence) {
            return new StringValue("'" + c + "'");
        } else if (c instanceof Date) {
            return new TimestampValue(Timestamp.valueOf(LocalDateTimeUtil.toLocalDateTime((Date) c)).toString());
        } else if (c instanceof Boolean) {
            return new LongValue(((Boolean) c) ? 1 : 0);
        } else {
            throw new IllegalArgumentException("暂不支持类型: " + (c != null ? c.getClass() : Void.class));
        }
    }
}
