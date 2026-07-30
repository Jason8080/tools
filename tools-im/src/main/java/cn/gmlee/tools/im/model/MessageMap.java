package cn.gmlee.tools.im.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于 HashMap 的默认消息实现.
 * <p>
 * 直接继承 {@link HashMap}，零额外字段。任意 JSON 请求体均可反序列化为此类型，
 * 开发者无需定义消息类即可使用框架。
 * </p>
 *
 * <h3>用法</h3>
 * <pre>{@code
 * MessageMap msg = new MessageMap();
 * msg.put("from", "alice");
 * msg.put("content", "hello");
 *
 * // 读取
 * String from = (String) msg.get("from");
 * }</pre>
 *
 * <h3>JSON 映射</h3>
 * <pre>{@code
 * // JSON: {"from": "alice", "content": "hello"}
 * // Java: MessageMap{"from"="alice", "content"="hello"}
 * }</pre>
 *
 * <h3>扩展方式</h3>
 * <p>
 * 如需强类型消息，可实现 {@link Msg} 接口自定义消息类。
 * </p>
 *
 * @since 5.6.0
 */
public class MessageMap extends HashMap<String, Object> implements Msg {
    /**
     * 空消息.
     */
    public MessageMap() {
    }

    /**
     * 基于已有 Map 创建消息.
     *
     * @param data 初始数据
     */
    public MessageMap(Map<String, Object> data) {
        super(data);
    }
}
