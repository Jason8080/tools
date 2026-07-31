package cn.gmlee.tools.im.endpoint;

import cn.gmlee.tools.im.conf.EndpointProperties;
import cn.gmlee.tools.im.spi.listener.EndpointChangeListener;
import cn.gmlee.tools.im.topic.TopicLifecycleManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 端点注册表.
 * <p>
 * 管理所有端点配置（path → {@link EndpointProperties}），支持启动时 YAML 加载和运行时动态增删。
 * 线程安全，所有操作通过 {@link ConcurrentHashMap} 保证原子性。
 * </p>
 *
 * <h3>核心职责</h3>
 * <ul>
 *   <li>路径解析：根据请求路径查找对应的端点配置</li>
 *   <li>生命周期管理：注册、注销、查询端点</li>
 *   <li>变更通知：注册/注销时触发回调（供 {@link EndpointChangeListener} 按需响应）</li>
 *   <li>Topic 引用管理：注册/注销时调用 {@link TopicLifecycleManager} 管理 Topic 引用计数</li>
 * </ul>
 *
 * @since 5.6.0
 */
@Slf4j
public class EndpointRegistry {

    /**
     * path → EndpointProperties 映射
     */
    private final Map<String, EndpointProperties> endpoints = new ConcurrentHashMap<>();

    /**
     * 变更监听器列表（线程安全）.
     * <p>
     * 使用 {@link CopyOnWriteArrayList} 保证并发安全性。
     * 适用于读多写少场景（启动时注册，运行时触发回调）。
     * </p>
     */
    private final List<EndpointChangeListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Topic 生命周期管理器（可选依赖，用于管理 Topic 引用计数）
     */
    private volatile TopicLifecycleManager topicLifecycleManager;

    /**
     * 设置 Topic 生命周期管理器.
     * <p>
     * 延迟注入，避免循环依赖。由自动配置类在初始化完成后调用。
     * </p>
     *
     * @param topicLifecycleManager Topic 生命周期管理器
     */
    public void setTopicLifecycleManager(TopicLifecycleManager topicLifecycleManager) {
        this.topicLifecycleManager = topicLifecycleManager;
    }

    /**
     * 注册端点.
     * <p>
     * 如果路径已存在，旧配置会被覆盖。注册成功后触发 {@link EndpointChangeListener#onEndpointRegistered} 回调。
     * 同时调用 {@link TopicLifecycleManager#acquire} 增加 Topic 引用计数。
     * </p>
     *
     * @param props 端点配置
     * @throws IllegalArgumentException 如果 props 或其必填字段为 null
     */
    public void register(EndpointProperties props) {
        validate(props);
        EndpointProperties old = endpoints.put(props.getPath(), props);
        if (old == null) {
            log.info("[EndpointRegistry] 注册端点: {} → topic={}, mode={}", props.getPath(), props.getTopic(), props.getMode());
            // 新增端点：增加 Topic 引用计数
            acquireTopic(props.getTopic());
        } else {
            log.info("[EndpointRegistry] 更新端点: {} → topic={}, mode={}", props.getPath(), props.getTopic(), props.getMode());
            // 更新端点：如果 Topic 变了，调整引用计数
            if (!old.getTopic().equals(props.getTopic())) {
                releaseTopic(old.getTopic());
                acquireTopic(props.getTopic());
            }
        }
        fireRegistered(props);
    }

    /**
     * 注销端点.
     * <p>
     * 注销成功后触发 {@link EndpointChangeListener#onEndpointUnregistered} 回调。
     * 同时调用 {@link TopicLifecycleManager#release} 减少 Topic 引用计数。
     * </p>
     *
     * @param path 请求路径
     * @return 被注销的端点配置，路径不存在返回 null
     */
    public EndpointProperties unregister(String path) {
        EndpointProperties removed = endpoints.remove(path);
        if (removed != null) {
            log.info("[EndpointRegistry] 注销端点: {}", path);
            // 减少 Topic 引用计数
            releaseTopic(removed.getTopic());
            fireUnregistered(removed);
        }
        return removed;
    }

    /**
     * 根据路径解析端点配置.
     *
     * @param path 请求路径
     * @return 端点配置，未注册返回 null
     */
    public EndpointProperties resolve(String path) {
        return endpoints.get(path);
    }

    /**
     * 获取所有已注册端点的快照.
     *
     * @return 端点配置集合（不可变视图）
     */
    public Collection<EndpointProperties> listAll() {
        return Collections.unmodifiableCollection(endpoints.values());
    }

    /**
     * 获取已注册端点数量.
     *
     * @return 端点数量
     */
    public int size() {
        return endpoints.size();
    }

    /**
     * 添加变更监听器.
     *
     * @param listener 监听器
     */
    public void addListener(EndpointChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除变更监听器.
     *
     * @param listener 监听器
     */
    public void removeListener(EndpointChangeListener listener) {
        listeners.remove(listener);
    }

    private void fireRegistered(EndpointProperties props) {
        for (EndpointChangeListener listener : listeners) {
            try {
                listener.onEndpointRegistered(props);
            } catch (Exception e) {
                log.error("[EndpointRegistry] 监听器 onEndpointRegistered 异常", e);
            }
        }
    }

    private void fireUnregistered(EndpointProperties props) {
        for (EndpointChangeListener listener : listeners) {
            try {
                listener.onEndpointUnregistered(props);
            } catch (Exception e) {
                log.error("[EndpointRegistry] 监听器 onEndpointUnregistered 异常", e);
            }
        }
    }

    private void validate(EndpointProperties props) {
        if (props == null) {
            throw new IllegalArgumentException("EndpointProperties 不能为 null");
        }
        if (props.getPath() == null || props.getPath().isEmpty()) {
            throw new IllegalArgumentException("path 不能为空");
        }
        if (props.getTopic() == null || props.getTopic().isEmpty()) {
            throw new IllegalArgumentException("topic 不能为空");
        }
        if (props.getMode() == null) {
            throw new IllegalArgumentException("mode 不能为 null");
        }
    }

    // ==================== Topic 引用计数管理 ====================

    /**
     * 增加 Topic 引用计数.
     *
     * @param topic Topic 名称
     */
    private void acquireTopic(String topic) {
        TopicLifecycleManager manager = this.topicLifecycleManager;
        if (manager != null) {
            try {
                manager.acquire(topic);
            } catch (Exception e) {
                log.error("[EndpointRegistry] Topic 引用计数递增失败: topic={}", topic, e);
            }
        }
    }

    /**
     * 减少 Topic 引用计数.
     *
     * @param topic Topic 名称
     */
    private void releaseTopic(String topic) {
        TopicLifecycleManager manager = this.topicLifecycleManager;
        if (manager != null) {
            try {
                manager.release(topic);
            } catch (Exception e) {
                log.error("[EndpointRegistry] Topic 引用计数递减失败: topic={}", topic, e);
            }
        }
    }
}
