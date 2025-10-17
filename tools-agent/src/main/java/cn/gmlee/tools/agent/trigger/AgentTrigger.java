package cn.gmlee.tools.agent.trigger;

import cn.gmlee.tools.agent.mod.Watcher;

/**
 * Agent触发器.
 */
public interface AgentTrigger {
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
}
