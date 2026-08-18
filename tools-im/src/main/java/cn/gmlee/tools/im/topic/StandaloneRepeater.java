package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.ImRepeater;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.spi.interceptor.RepeaterInterceptor;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.Serializable;
import java.util.List;

/**
 * 单机模式消息转发器（包级私有）.
 * <p>
 * 继承 {@link ImRepeater} 框架骨架，在 STANDALONE 部署模式下使用。
 * 绕过 MQ，直接通过内存传递消息，实现极低延迟（&lt;0.1ms）。
 * </p>
 *
 * <h3>核心设计</h3>
 * <ul>
 *   <li><b>绕过 MQ</b>：{@link #doSend(TopicMessage)} 直接调用 {@link #receive(TopicMessage)}，
 *       而不是通过 {@code StreamBridge} 发送到 MQ</li>
 *   <li><b>拦截器一致性</b>：调用 {@code receive()} 而不是 {@code doReceive()}，
 *       保证 {@code afterReceive} 拦截器被正确执行，与 CLUSTER 模式行为完全一致</li>
 *   <li><b>线程隔离</b>：使用 {@code subscribeOn(Schedulers.boundedElastic())} 切换到独立线程池，
 *       避免阻塞 WebFlux 事件循环</li>
 *   <li><b>错误隔离</b>：错误被 {@link Mono} 捕获，不会传播到调用方</li>
 * </ul>
 *
 * <h3>SPI 行为一致性</h3>
 * <p>
 * 除 MQ 传递路径外，所有 SPI 行为与 CLUSTER 模式完全一致：
 * </p>
 * <ul>
 *   <li>✅ {@code beforeSend} 拦截器 - 在 {@code send()} 中调用</li>
 *   <li>✅ {@code afterReceive} 拦截器 - 在 {@code receive()} 中调用</li>
 *   <li>✅ {@code transformSubscribeStream} 拦截器 - 在 {@code subscribe()} 中调用</li>
 *   <li>✅ 工厂模式 - 通过 {@link cn.gmlee.tools.im.spi.factory.RepeaterFactory} 创建</li>
 *   <li>✅ 所有其他 SPI 扩展点</li>
 * </ul>
 *
 * <h3>数据流</h3>
 * <pre>
 * send() → beforeSend → doSend() → receive() → doReceive() → afterReceive
 *                                         ↑
 *                                     直接调用（绕过 MQ）
 * </pre>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>单机部署，无需集群支持</li>
 *   <li>对延迟极度敏感的场景</li>
 *   <li>开发/测试环境，简化依赖</li>
 * </ul>
 *
 * <h3>限制</h3>
 * <ul>
 *   <li>❌ 不支持多实例集群部署</li>
 *   <li>❌ 消息不会持久化，应用重启后丢失</li>
 *   <li>✅ 所有 SPI 扩展点正常工作</li>
 * </ul>
 *
 * @since 5.6.0
 * @see ClusterTopicResourceFactory
 * @see DeploymentMode#STANDALONE
 */
@Slf4j
class StandaloneRepeater extends ImRepeater<Serializable, Msg> {

    /**
     * 创建单机模式 Repeater.
     *
     * @param topic                Topic 名称
     * @param sseConnectionManager SSE 连接管理器（提供 publish/subscribe 函数）
     * @param interceptors         拦截器列表（可为 null）
     */
    public StandaloneRepeater(String topic,
                              SseConnectionManager sseConnectionManager,
                              List<RepeaterInterceptor> interceptors) {
        super(topic, null,  // 不需要 StreamBridge
              sseConnectionManager::publish,
              sseConnectionManager::subscribe,
              interceptors);
    }

    /**
     * 单机模式发送逻辑：直接调用 receive()，绕过 MQ.
     * <p>
     * <b>关键设计</b>：调用 {@link #receive(TopicMessage)} 而不是 {@link #doReceive(TopicMessage)}，
     * 保证 {@code afterReceive} 拦截器被正确执行，与 CLUSTER 模式行为完全一致。
     * </p>
     * <p>
     * <b>线程隔离</b>：使用 {@code subscribeOn(Schedulers.boundedElastic())} 切换到独立线程池，
     * 避免阻塞 WebFlux 事件循环。{@code boundedElastic} 线程池有上限，防止资源耗尽。
     * </p>
     *
     * @param message 消息信封
     * @return 消息 ID（异步）
     */
    @Override
    protected Mono<Serializable> doSend(TopicMessage message) {
        return Mono.fromRunnable(() -> {
            // 调用 receive() 而不是 doReceive()
            // 保证 afterReceive 拦截器被执行
            receive(message);
            log.debug("[StandaloneRepeater] 本地转发: topic={}, id={}", topic(), message.getId());
        })
        .subscribeOn(Schedulers.boundedElastic())  // 线程隔离，避免阻塞事件循环
        .thenReturn(message.getId());
    }
}
