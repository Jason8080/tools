package cn.gmlee.tools.prevent.utils;

import cn.gmlee.tools.prevent.constant.SlideWindowLuaConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * SlideWindowUtils
 *
 * @author James
 * @since 2023/7/27 18:32
 */
@Slf4j
public class SlideWindowUtils {

    private final StringRedisTemplate stringRedisTemplate;

    public SlideWindowUtils(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean isPass(String key, long count, long timeWindow) {
        if (count <= 0 || timeWindow <= 0) {
            return false;
        }
        Long eval = stringRedisTemplate.execute(
                new DefaultRedisScript<>(SlideWindowLuaConstant.SLIDE_WINDOW_LUA, Long.class),
                Collections.singletonList(key),
                String.valueOf(count - 1), String.valueOf(timeWindow), String.valueOf(System.currentTimeMillis()));
        return Long.valueOf(1L).equals(eval);
    }
}
