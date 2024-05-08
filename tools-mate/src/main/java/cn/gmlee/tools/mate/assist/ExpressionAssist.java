package cn.gmlee.tools.mate.assist;

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
    public static List<Expression> as(Comparable... cs) {
        if (cs == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(cs).filter(Objects::nonNull).map(ExpressionAssist::convert).collect(Collectors.toList());
    }

    private static Expression convert(Comparable c) {
        if (c instanceof Integer || c instanceof Long) {
            return new LongValue(c.toString());
        } else if (c instanceof Float || c instanceof Double) {
            return new DoubleValue(c.toString());
        } else if (c instanceof String) {
            return new StringValue("'" + c + "'");
        } else if (c instanceof Date) {
            return new TimestampValue(Timestamp.valueOf(LocalDateTimeUtil.toLocalDateTime((Date) c)).toString());
        } else if (c instanceof Boolean) {
            return new LongValue(((Boolean) c) ? 1 : 0);
        } else {
            throw new IllegalArgumentException("暂不支持类型: " + c.getClass());
        }
    }
}
