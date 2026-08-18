package cn.gmlee.tools.im.endpoint;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.im.conf.EndpointProperties;
import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.sse.SseConnection;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.sse.metrics.SseMetrics;
import cn.gmlee.tools.im.topic.TopicLifecycleManager;
import cn.gmlee.tools.im.topic.TopicRegistry;
import cn.gmlee.tools.im.model.TopicState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * IM 框架运行时管理接口.
 * <p>
 * 提供端点注册表、Topic 状态、连接信息的查询 API，用于监控、运维、调试。
 * 所有接口返回 JSON 格式，统一使用 {@link R} 包装响应。
 * </p>
 *
 * <h3>安全说明</h3>
 * <p>
 * 此控制器暴露框架内部状态，生产环境应通过 {@link cn.gmlee.tools.im.spi.access.AccessFilter}
 * 添加访问控制（认证、授权、IP 白名单等）。
 * </p>
 *
 * <h3>接口列表</h3>
 * <ul>
 *   <li>{@code GET /im/admin/endpoints} — 查询所有已注册端点</li>
 *   <li>{@code GET /im/admin/topics} — 查询所有活跃 Topic 及连接数</li>
 *   <li>{@code GET /im/admin/topics/lifecycle} — 查询 Topic 生命周期状态（v5.6.0+）</li>
 *   <li>{@code GET /im/admin/connections/{topic}} — 查询指定 Topic 的连接详情</li>
 *   <li>{@code GET /im/admin/stats} — 查询全局统计信息</li>
 *   <li>{@code DELETE /im/admin/topic/{topic}} — 手动销毁 Topic（v5.6.0+）</li>
 * </ul>
 *
 * @since 5.6.0
 */
@Slf4j
@RestController
@RequestMapping("/im/admin")
public class ImAdminController {

    private final EndpointRegistry endpointRegistry;
    private final SseConnectionManager connectionManager;
    private final TopicRegistry topicRegistry;
    private final SseMetrics metrics;
    private final TopicLifecycleManager topicLifecycleManager;

    /**
     * 创建管理控制器.
     *
     * @param endpointRegistry      端点注册表
     * @param connectionManager     SSE 连接管理器
     * @param topicRegistry         Topic 组件注册表
     * @param metrics               指标收集器（可为 null）
     * @param topicLifecycleManager Topic 生命周期管理器（可为 null，向后兼容）
     */
    public ImAdminController(EndpointRegistry endpointRegistry,
                             SseConnectionManager connectionManager,
                             TopicRegistry topicRegistry,
                             SseMetrics metrics,
                             @Autowired(required = false) TopicLifecycleManager topicLifecycleManager) {
        this.endpointRegistry = endpointRegistry;
        this.connectionManager = connectionManager;
        this.topicRegistry = topicRegistry;
        this.metrics = metrics;
        this.topicLifecycleManager = topicLifecycleManager;
    }

    /**
     * 查询所有已注册端点.
     *
     * @return 端点配置列表
     */
    @GetMapping("/endpoints")
    public R<List<Map<String, Object>>> listEndpoints() {
        Collection<EndpointProperties> endpoints = endpointRegistry.listAll();
        List<Map<String, Object>> result = endpoints.stream()
                .map(this::toEndpointMap)
                .collect(Collectors.toList());
        return R.of(result);
    }

    /**
     * 查询所有活跃 Topic 及连接数.
     *
     * @return Topic 列表（包含 topic 名称和连接数）
     */
    @GetMapping("/topics")
    public R<List<Map<String, Object>>> listTopics() {
        Set<String> topics = connectionManager.getAllTopics();
        List<Map<String, Object>> result = topics.stream()
                .map(topic -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("topic", topic);
                    map.put("connections", connectionManager.getConnectionCount(topic));
                    return map;
                })
                .sorted(Comparator.comparingLong((Map<String, Object> m) -> ((Number) m.get("connections")).longValue()).reversed())
                .collect(Collectors.toList());
        return R.of(result);
    }

    /**
     * 查询指定 Topic 的连接详情.
     *
     * @param topic Topic 名称
     * @return 连接详情列表
     */
    @GetMapping("/connections/{topic}")
    public R<List<Map<String, Object>>> getConnections(@PathVariable String topic) {
        List<SseConnection> connections = connectionManager.getConnectionDetails(topic);
        List<Map<String, Object>> result = connections.stream()
                .map(this::toConnectionMap)
                .collect(Collectors.toList());
        return R.of(result);
    }

    /**
     * 查询全局统计信息.
     *
     * @return 统计信息（总连接数、Topic 数、端点数等）
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalConnections", connectionManager.getTotalConnections());
        stats.put("activeTopics", connectionManager.getAllTopics().size());
        stats.put("registeredEndpoints", endpointRegistry.size());
        stats.put("managerRunning", connectionManager.isRunning());
        stats.put("managerClosed", connectionManager.isClosed());

        stats.put("metricsAvailable", metrics != null);

        return R.of(stats);
    }

    /**
     * 查询 Topic 生命周期状态.
     * <p>
     * 返回所有 Topic 的状态（CREATED/ACTIVE/DESTROYING/DESTROYED）、引用计数、
     * 关联的物理资源信息等。用于运维监控 Topic 资源使用情况。
     * </p>
     *
     * @return Topic 生命周期信息列表（TopicLifecycleManager 未启用时返回空列表）
     * @since 5.6.0
     */
    @GetMapping("/topics/lifecycle")
    public R<List<Map<String, Object>>> listTopicsLifecycle() {
        if (topicLifecycleManager == null) {
            return R.of(Collections.emptyList());
        }
        Set<String> topics = topicLifecycleManager.getAllTopics();
        List<Map<String, Object>> result = topics.stream()
                .map(this::toTopicLifecycleMap)
                .sorted(Comparator.comparingLong((Map<String, Object> m) -> ((Number) m.get("refCount")).longValue()).reversed())
                .collect(Collectors.toList());
        return R.of(result);
    }

    /**
     * 手动销毁 Topic.
     * <p>
     * 强制销毁指定 Topic 的所有资源（Publisher/Repeater/Subscriber、Spring Cloud Stream binding），
     * 忽略引用计数。适用于资源泄漏后的紧急清理场景。
     * </p>
     * <p><b>注意</b>：销毁后仍在使用该 Topic 的端点将无法发送/接收消息，需重新注册端点以激活 Topic。</p>
     *
     * @param topic Topic 名称
     * @return 销毁结果（success / not_found / failed）
     * @since 5.6.0
     */
    @DeleteMapping("/topic/{topic}")
    public R<Map<String, Object>> destroyTopic(@PathVariable String topic) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("topic", topic);

        if (topicLifecycleManager == null) {
            result.put("success", false);
            result.put("reason", "TopicLifecycleManager 未启用");
            return R.of(result);
        }

        TopicState state = topicLifecycleManager.getState(topic);
        if (state == null) {
            result.put("success", false);
            result.put("reason", "Topic 不存在");
            return R.of(result);
        }

        boolean destroyed = topicLifecycleManager.destroy(topic);
        result.put("success", destroyed);
        result.put("previousState", state.name());
        return R.of(result);
    }

    /**
     * 将 Topic 生命周期信息转换为 Map.
     */
    private Map<String, Object> toTopicLifecycleMap(String topic) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("topic", topic);

        TopicState state = topicLifecycleManager.getState(topic);
        map.put("state", state != null ? state.name() : "UNKNOWN");
        map.put("refCount", topicLifecycleManager.getRefCount(topic));

        // 关联的连接数（从 ConnectionManager 获取）
        map.put("connections", connectionManager.getConnectionCount(topic));

        // 物理资源状态
        map.put("hasRegistryComponents", topicRegistry.hasComponents(topic));

        return map;
    }

    /**
     * 将端点配置转换为 Map（便于 JSON 序列化）.
     */
    private Map<String, Object> toEndpointMap(EndpointProperties props) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("path", props.getPath());
        map.put("topic", props.getTopic());
        map.put("mode", props.getMode().name());
        return map;
    }

    /**
     * 将连接信息转换为 Map（便于 JSON 序列化）.
     */
    private Map<String, Object> toConnectionMap(SseConnection conn) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("connectionId", conn.getConnectionId());
        map.put("topic", conn.getTopic());
        map.put("state", conn.getState().get().name());
        map.put("createdAt", conn.getCreatedAt().toString());

        ConnectionMetadata metadata = conn.getMetadata();
        if (metadata != null) {
            map.put("routingKey", metadata.getRoutingKey());
        }

        // 计算连接时长
        Duration age = Duration.between(conn.getCreatedAt(), Instant.now());
        map.put("ageSeconds", age.getSeconds());

        return map;
    }
}
