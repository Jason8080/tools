package cn.gmlee.tools.profile.helper;

import cn.gmlee.tools.base.util.WebUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * 环境帮助类.
 */
public class ProfileHelper {

    private static volatile Boolean open = Boolean.TRUE;

    private static ThreadLocal<Map<ReadWrite, Set<Env>>> env = new InheritableThreadLocal<>();

    /**
     * The enum Read write.
     */
    @Getter
    public enum ReadWrite {
        /**
         * Read read write.
         */
        READ,
        /**
         * Write read write.
         */
        WRITE,
        ;
    }

    /**
     * The enum Env.
     */
    @RequiredArgsConstructor
    public enum Env {
        /**
         * Stg env.
         */
        STG(0),
        /**
         * Prd env.
         */
        PRD(1),
        ;
        /**
         * The Value.
         */
        public final Integer value;
    }

    /**
     * 添加环境.
     *
     * @param rw   the rw
     * @param envs the envs
     */
    public static void add(ReadWrite rw, Env... envs) {
        Map<ReadWrite, Set<Env>> map = env.get();
        if (map == null) {
            map = new HashMap<>();
            env.set(map);
        }
        Set<Env> set = map.get(rw);
        if (set == null) {
            set = new HashSet<>();
            map.put(rw, set);
        }
        if (envs != null && envs.length > 0) {
            set.addAll(Arrays.asList(envs));
        }
    }

    /**
     * 获取环境.
     *
     * @param rw the rw
     * @return set set
     */
    public static Set<Env> get(ReadWrite rw) {
        Map<ReadWrite, Set<Env>> map = env.get();
        if (map == null) {
            return Collections.emptySet();
        }
        Set<Env> set = map.get(rw);
        if (set == null) {
            return Collections.emptySet();
        }
        return set;
    }

    /**
     * 禁用.
     *
     * @return the boolean
     */
    public static void remove() {
        ProfileHelper.env.remove();
    }

    /**
     * 是否启用STG环境.
     *
     * @param rw the rw
     * @return the boolean
     */
    public static boolean enabled(ReadWrite rw) {
        Set<Env> envs = get(rw);
        return envs.contains(Env.STG);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 开关.
     *
     * @param open the open
     * @return the boolean
     */
    public static void open(boolean open) {
        ProfileHelper.open = open;
    }

    /**
     * 是否已关闭.
     *
     * @return the boolean
     */
    public static boolean closed() {
        return !Boolean.TRUE.equals(open);
    }
}
