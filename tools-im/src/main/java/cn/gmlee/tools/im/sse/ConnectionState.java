package cn.gmlee.tools.im.sse;

/**
 * SSE 连接状态枚举.
 * <p>
 * 状态转换规则：
 * <ul>
 *   <li>CREATED → ACTIVE：首次发送数据或心跳</li>
 *   <li>ACTIVE → DRAINING：空闲超时 / 管理员断开 / 服务关闭</li>
 *   <li>ACTIVE → CLOSED：客户端取消 / 错误 / 连接重置</li>
 *   <li>DRAINING → CLOSED：drain 超时 / 最终消息已发送</li>
 * </ul>
 * 任何状态均可通过错误/取消直接转为 CLOSED。
 * </p>
 */
public enum ConnectionState {

    /**
     * 已创建：subscribe() 已调用，限制检查通过
     */
    CREATED,

    /**
     * 活跃中：客户端正在接收数据/心跳
     */
    ACTIVE,

    /**
     * 排空中：优雅关闭进行中
     */
    DRAINING,

    /**
     * 已关闭：资源已释放，计数器已递减（终态）
     */
    CLOSED
}
