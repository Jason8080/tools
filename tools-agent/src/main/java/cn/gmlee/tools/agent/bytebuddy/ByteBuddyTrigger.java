package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.mod.Watcher;

/**
 * ByteBuddy触发器.
 */
public interface ByteBuddyTrigger {
    /**
     * 进入.
     *
     * @param watcher the watcher
     */
    void enter(Watcher watcher);

    /**
     * 退出.
     *
     * @param watcher the watcher
     */
    void exit(Watcher watcher);

    /**
     * 超时.
     *
     * @param watcher the watcher
     */
    void timout(Watcher watcher);
}
