package cn.gmlee.tools.prevent.constant;

/**
 * @desc: 滑动窗口限流Lua脚本常量
 *
 * @author: James
 * @date: 2023/7/27 18:03
 */
public interface SlideWindowLuaConstant {

    /**
     * 设置 Redis List 的过期时间为 time_window + 1000 毫秒的目的是为了在滑动窗口中保持一定的宽限期，防止在边界情况下出现请求被拒绝的问题。
     *
     * 考虑以下情况：假设滑动窗口的时间窗口大小为 time_window 秒，窗口内允许通过的请求数量为 n 个。
     * 在某个时刻，滑动窗口的最早时间戳在当前时间窗口的最后一秒内，此时将有 n-1 个请求通过限流，还有 1 个请求的时间戳处于时间窗口的倒数第二秒。
     *
     * 如果不设置 Redis List 的过期时间，那么在这个时刻之后，窗口内就只有 n-1 个时间戳了，当新的请求到来时，根据滑动窗口算法，这个时间戳会被移除，
     * 导致实际窗口内的请求数量小于 n-1，即小于允许通过的请求数量。这样会导致请求被错误地拒绝。
     *
     * 为了避免这种情况，我们可以设置 Redis List 的过期时间为 time_window + 1000 毫秒，即时间窗口大小加上一个较小的宽限期。
     * 这样在滑动窗口满了之后，即使最早的时间戳被移除了，窗口内还有一段时间内的时间戳可以继续保持在 Redis List 中，
     * 确保窗口内的请求数量能够正确地维持在 n 个。
     *
     * 设置较小的宽限期（1000 毫秒）是为了避免在边界情况下请求被错误地拒绝，但又不过度增加 Redis 数据的存储和管理开销。
     * 通过合理设置过期时间，我们可以保证滑动窗口限流算法的正确性和稳定性。
     */

    String SLIDE_WINDOW_LUA = "-- 获取key\n" +
            "local key = KEYS[1];\n" +
            "-- 当前时间窗口的索引 用于在 Redis List 中定位时间窗口\n" +
            "local index = tonumber(ARGV[1]);\n" +
            "-- 滑动窗口的时间窗口大小\n" +
            "local time_window = tonumber(ARGV[2]);\n" +
            "-- 当前请求的时间戳\n" +
            "local now_time = tonumber(ARGV[3]);\n" +
            "-- 时间窗口的最早时间戳\n" +
            "local far_time = redis.call('lindex', key, index);\n" +
            "-- 如果 far_time 为空 表示 Redis List 为空或者索引index处不存在时间戳\n" +
            "if (not far_time) then\n" +
            "-- 当前时间戳 now_time 加入到 Redis List key 的最左侧（头部）\n" +
            "  redis.call('lpush', key, now_time);\n" +
            "-- 设置过期时间 保证List的大小不超过窗口大小\n" +
            "  redis.call('pexpire', key, time_window + 1000);\n" +
            "-- 返回 1 表示允许通过请求\n" +
            "  return 1;\n" +
            "end\n" +
            "-- far_time 不为空 判断 now_time 减去 far_time 是否大于 time_window\n" +
            "if (now_time - far_time > time_window) then\n" +
            "-- 将最早时间戳从 Redis List 的最右侧（尾部）移除\n" +
            "  redis.call('rpop', key);\n" +
            "-- 将当前时间戳 now_time 加入到 Redis List key 的最左侧（头部）\n" +
            "  redis.call('lpush', key, now_time);\n" +
            "-- 更新 Redis List 的过期时间为 time_window + 1000 毫秒\n" +
            "  redis.call('pexpire', key, time_window + 1000);\n" +
            "  return 1;\n" +
            "else\n" +
            "-- 返回0 不允许通过\n" +
            "  return 0;\n" +
            "end";

}
