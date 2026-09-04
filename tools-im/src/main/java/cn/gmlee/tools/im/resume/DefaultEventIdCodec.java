package cn.gmlee.tools.im.resume;

import java.io.Serializable;

/**
 * 默认事件 ID 编解码器（数字优先）.
 * <p>
 * 编码：{@code String.valueOf(id)}。
 * 解码：纯数字（可带负号）解析为 {@link Long}，超出 Long 范围或非数字时保留为 {@link String}。
 * </p>
 * <p>
 * 与框架默认 ID 生成策略（单 JVM 自增 {@code Long}）及
 * {@link SnowflakeEventIdGenerator} 天然匹配。
 * </p>
 *
 * @since 5.7.0
 */
final class DefaultEventIdCodec implements EventIdCodec {

    @Override
    public String encode(Serializable id) {
        return id == null ? null : String.valueOf(id);
    }

    @Override
    public Serializable decode(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (isNumeric(trimmed)) {
            try {
                return Long.parseLong(trimmed);
            } catch (NumberFormatException e) {
                // 超出 Long 范围（如自定义大数 ID），按字符串处理
                return trimmed;
            }
        }
        return trimmed;
    }

    /**
     * 判断是否为（可带负号的）纯数字.
     */
    private static boolean isNumeric(String s) {
        int start = 0;
        if (s.charAt(0) == '-') {
            if (s.length() == 1) {
                return false;
            }
            start = 1;
        }
        for (int i = start; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
