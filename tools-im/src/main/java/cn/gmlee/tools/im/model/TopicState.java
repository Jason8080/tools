package cn.gmlee.tools.im.model;

/**
 * Topic 生命周期状态.
 * <p>
 * 状态转换：
 * <pre>
 * CREATED → ACTIVE → DESTROYING → DESTROYED
 *             ↑                      ↓
 *             └──────────────────────┘
 *                   (可重新激活)
 * </pre>
 * </p>
 *
 * @since 5.6.0
 */
public enum TopicState {

    /**
     * 已创建（组件已初始化，但可能还没有连接）
     */
    CREATED,

    /**
     * 活跃（至少有一个连接或正在使用）
     */
    ACTIVE,

    /**
     * 销毁中（正在执行清理流程）
     */
    DESTROYING,

    /**
     * 已销毁（所有资源已释放）
     */
    DESTROYED
}
