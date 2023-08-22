package cn.gmlee.tools.prevent.utils;

import cn.gmlee.tools.prevent.constant.SlideWindowLuaConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

/**
 * @desc: SlideWindowUtils
 *
 * @author: James
 * @date: 2023/7/27 18:32
 */
@Slf4j
public class SlideWindowUtils {

    private StringRedisTemplate stringRedisTemplate;

    public SlideWindowUtils(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean isPass(String key, long count, long timeWindow) {
        if (count <= 0 || timeWindow <= 0) {
            return false;
        }
        long eval = stringRedisTemplate.execute(
                new DefaultRedisScript<Long>(SlideWindowLuaConstant.SLIDE_WINDOW_LUA, Long.class),
                Collections.singletonList(key),
                String.valueOf(count - 1), String.valueOf(timeWindow), String.valueOf(System.currentTimeMillis()));
        return Long.valueOf(1L).equals(eval);
    }
}
