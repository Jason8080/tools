package cn.gmlee.tools.im.resume;

import java.io.Serializable;

/**
 * 默认事件 ID 顺序比较器.
 * <p>
 * 判定规则（按优先级）：
 * </p>
 * <ol>
 *   <li>水位线为 null → 通过</li>
 *   <li>候选为 null → 通过（宁重勿漏）</li>
 *   <li>双方均为 {@link Number} → 按 {@code longValue} 比较（ID 必须为整数语义）</li>
 *   <li>同类型且实现 {@link Comparable} → 按 {@code compareTo} 比较</li>
 *   <li>其余（类型不一致 / 不可比较）→ 通过（宁重勿漏）</li>
 * </ol>
 *
 * @since 5.7.0
 */
final class DefaultEventIdComparator implements EventIdComparator {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean isAfter(Serializable candidate, Serializable watermark) {
        if (watermark == null) {
            return true;
        }
        if (candidate == null) {
            return true;
        }
        if (candidate instanceof Number nc && watermark instanceof Number nw) {
            return nc.longValue() > nw.longValue();
        }
        if (candidate.getClass() == watermark.getClass() && candidate instanceof Comparable cc) {
            try {
                return cc.compareTo(watermark) > 0;
            } catch (Exception e) {
                return true;
            }
        }
        return true;
    }
}
