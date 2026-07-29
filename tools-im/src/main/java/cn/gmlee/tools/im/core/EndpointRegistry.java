package cn.gmlee.tools.im.core;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 端点注册表.
 * <p>
 * 管理所有端点配置（path → {@link EndpointConfig}），支持启动时 YAML 加载和运行时动态增删。
 * 线程安全，所有操作通过 {@link ConcurrentHashMap} 保证原子性。
 * </p>
 *
 * <h3>核心职责</h3>
 * <ul>
 *   <li>路径解析：根据请求路径查找对应的端点配置</li>
 *   <li>生命周期管理：注册、注销、查询端点</li>
 *   <li>变更通知：注册/注销时触发回调（供 {@code TopicFactory} 按需创建资源）</li>
 * </ul>
 *
 * @since 5.6.0
 */
@Slf4j
public class EndpointRegistry {

    /**
     * path → EndpointConfig 映射
     */
    private final Map<String, EndpointConfig> endpoints = new ConcurrentHashMap<>();

    /**
     * 变更监听器列表
     */
    private final List<ChangeListener> listeners = new ArrayList<>();

    /**
     * 注册端点.
     * <p>
     * 如果路径已存在，旧配置会被覆盖。注册成功后触发 {@link ChangeListener#onEndpointRegistered} 回调。
     * </p>
     *
     * @param config 端点配置
     * @throws IllegalArgumentException 如果 config 或其必填字段为 null
     */
    public void register(EndpointConfig config) {
        validate(config);
        EndpointConfig old = endpoints.put(config.getPath(), config);
        if (old == null) {
            log.info("[EndpointRegistry] 注册端点: {} → topic={}, mode={}", config.getPath(), config.getTopic(), config.getMode());
        } else {
            log.info("[EndpointRegistry] 更新端点: {} → topic={}, mode={}", config.getPath(), config.getTopic(), config.getMode());
        }
        fireRegistered(config);
    }

    /**
     * 注销端点.
     * <p>
     * 注销成功后触发 {@link ChangeListener#onEndpointUnregistered} 回调。
     * </p>
     *
     * @param path 请求路径
     * @return 被注销的端点配置，路径不存在返回 null
     */
    public EndpointConfig unregister(String path) {
        EndpointConfig removed = endpoints.remove(path);
        if (removed != null) {
            log.info("[EndpointRegistry] 注销端点: {}", path);
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
    public EndpointConfig resolve(String path) {
        return endpoints.get(path);
    }

    /**
     * 获取所有已注册端点的快照.
     *
     * @return 端点配置集合（不可变视图）
     */
    public Collection<EndpointConfig> listAll() {
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
    public void addListener(ChangeListener listener) {
        listeners.add(listener);
    }

    private void fireRegistered(EndpointConfig config) {
        for (ChangeListener listener : listeners) {
            try {
                listener.onEndpointRegistered(config);
            } catch (Exception e) {
                log.error("[EndpointRegistry] 监听器 onEndpointRegistered 异常", e);
            }
        }
    }

    private void fireUnregistered(EndpointConfig config) {
        for (ChangeListener listener : listeners) {
            try {
                listener.onEndpointUnregistered(config);
            } catch (Exception e) {
                log.error("[EndpointRegistry] 监听器 onEndpointUnregistered 异常", e);
            }
        }
    }

    private void validate(EndpointConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("EndpointConfig 不能为 null");
        }
        if (config.getPath() == null || config.getPath().isEmpty()) {
            throw new IllegalArgumentException("path 不能为空");
        }
        if (config.getTopic() == null || config.getTopic().isEmpty()) {
            throw new IllegalArgumentException("topic 不能为空");
        }
        if (config.getMode() == null) {
            throw new IllegalArgumentException("mode 不能为 null");
        }
    }

    /**
     * 端点变更监听器.
     */
    public interface ChangeListener {

        /**
         * 端点注册后触发.
         *
         * @param config 新注册的端点配置
         */
        default void onEndpointRegistered(EndpointConfig config) {}

        /**
         * 端点注销后触发.
         *
         * @param config 被注销的端点配置
         */
        default void onEndpointUnregistered(EndpointConfig config) {}
    }
}
