# SseIm Framework

> 通用实时消息推送开发框架 — 基于 Spring Cloud Stream 的声明式 SSE 推送框架

## 核心特性

- **Topic 驱动** — 所有消息围绕 Topic 组织，发布者和订阅者通过 Topic 解耦
- **声明式编程** — 注解 + 接口，无需编写 Controller / 连接管理代码
- **零基础设施代码** — 心跳、重连、背压、多实例同步全部自动化
- **动态 SSE Endpoint** — 框架自动为每个 Topic 生成 `/sse/{topic}` 端点
- **Broker 无关** — 基于 Spring Cloud Stream 抽象，支持 RabbitMQ / Kafka / 任意 Binder
- **通配符订阅** — 支持 `*`（单层）和 `#`（多层）模式匹配

## 架构

```
┌──────────────────────────────────────────────────────────────────────┐
│                      DEVELOPER LAYER (开发者层)                       │
│  TopicPublisher.publish()    TopicSubscriber / @TopicSubscription    │
└──────────────────────────────────────────────────────────────────────┘
                                    │
┌──────────────────────────────────────────────────────────────────────┐
│                      FRAMEWORK LAYER (框架层)                         │
│                                                                      │
│  ┌──────────┐    ┌──────────┐    ┌──────────────────────────────┐   │
│  │ Topic    │───►│ Topic    │───►│ SseTopicSubscriber           │   │
│  │ Publisher│    │ Router   │    │   └──► SseConnectionManager  │   │
│  └──────────┘    └──────────┘    │        └──► SseController    │   │
│       │                          └──────────────────────────────┘   │
│       │                                                              │
│       ▼  (多实例模式)                                                 │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ Spring Cloud Stream (Broker 无关)                             │   │
│  │  StreamBridgeBroadcaster ──► Stream ──► StreamBroadcastConsumer│  │
│  └──────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              ▼                     ▼                     ▼
        ┌───────────┐        ┌───────────┐        ┌───────────┐
        │ RabbitMQ  │        │   Kafka   │        │  其他     │
        │  Binder   │        │  Binder   │        │  Binder   │
        └───────────┘        └───────────┘        └───────────┘
```

## 快速开始

### 1. 添加依赖

```xml
<!-- 框架核心 -->
<dependency>
    <groupId>cn.gmlee.tools</groupId>
    <artifactId>tools-im</artifactId>
    <version>5.6.0-SNAPSHOT</version>
</dependency>

<!-- 选择你的 Broker Binder（二选一） -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-rabbit</artifactId>
</dependency>
<!-- 或 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-kafka</artifactId>
</dependency>
```

### 2. 推送消息（一行代码）

```java
@Service
public class StockService {
    @Autowired
    private TopicPublisher publisher;

    public void updatePrice(String symbol, BigDecimal price) {
        publisher.publish("stock." + symbol, new StockPrice(symbol, price));
    }
}
```

### 3. 订阅消息（接口方式）

```java
@Component
public class PriceHandler implements TopicSubscriber<StockPrice> {
    @Override
    public String getTopic() { return "stock.*"; }

    @Override
    public void onMessage(String topic, StockPrice price) {
        log.info("{}: ${}", price.getSymbol(), price.getPrice());
    }
}
```

### 4. 订阅消息（注解方式）

```java
@Component
public class AlertHandler {
    @TopicSubscription("stock.*")
    public void onPriceChange(String topic, StockPrice price) {
        if (Math.abs(price.getChange()) > 1.0) {
            log.warn("ALERT: {} changed significantly!", topic);
        }
    }
}
```

### 5. 客户端订阅（自动可用）

```javascript
// 框架自动生成 SSE endpoint，客户端直接订阅
const es = new EventSource('/sse/stock.AAPL');
es.addEventListener('message', e => {
    const data = JSON.parse(e.data);
    console.log('Price:', data.payload.price);
});
```

## 配置

### 基础配置

```yaml
sse-im:
  sse:
    enabled: true              # 启用 SSE
    base-path: /sse            # SSE 端点前缀
    heartbeat-interval: 15s    # 心跳间隔
    max-connections-per-topic: 10000
    buffer-size: 1024          # Sinks.Many 缓冲区
    retry-millis: 5000         # 客户端重连间隔
```

### 多实例同步配置（Spring Cloud Stream）

```yaml
sse-im:
  stream:
    enabled: true
    broadcast-destination: sse-im-broadcast
    consumer-prefetch: 250     # 覆盖 SCS 默认的 1（性能关键！）
    consumer-concurrency: 4
    skip-self-messages: true

spring:
  cloud:
    function:
      definition: sseImBroadcastConsumer
    stream:
      bindings:
        sseImBroadcastConsumer-in-0:
          destination: sse-im-broadcast
          # 不设 group → Fanout 广播到所有实例

      # ---- RabbitMQ Binder 调优 ----
      rabbit:
        bindings:
          sseImBroadcastConsumer-in-0:
            consumer:
              prefetch: 250
              max-concurrency: 4

      # ---- Kafka Binder 调优 ----
      # kafka:
      #   bindings:
      #     sseImBroadcastConsumer-in-0:
      #       consumer:
      #         concurrency: 4
```

## API 参考

### TopicPublisher

| 方法 | 说明 |
|------|------|
| `publish(topic, message)` | 发布消息到指定 Topic |
| `publish(topics, message)` | 发布到多个 Topic |
| `publish(topic, message, metadata)` | 带元数据发布 |
| `publishBatch(topic, messages)` | 批量发布 |
| `publishAsync(topic, message)` | 异步发布 |
| `hasSubscribers(topic)` | 检查是否有订阅者 |

### TopicSubscriber\<T\>

| 方法 | 说明 | 必填 |
|------|------|------|
| `getTopic()` | 订阅的 Topic（支持通配符） | ✅ |
| `onMessage(topic, message)` | 处理消息 | ✅ |
| `onError(topic, error)` | 错误处理 | — |
| `getExecutor()` | 自定义线程池 | — |
| `getPriority()` | 优先级 | — |

### @TopicSubscription

| 属性 | 类型 | 说明 |
|------|------|------|
| `value` | String | Topic（精确匹配） |
| `pattern` | String | Topic 模式（通配符） |
| `priority` | int | 优先级 |
| `async` | boolean | 异步处理 |

### SSE Endpoints

| Endpoint | 说明 |
|----------|------|
| `GET /sse/{topic}` | 订阅单个 Topic |
| `GET /sse/events?pattern=xxx` | 通配符订阅 |
| `GET /sse/info/{topic}` | Topic 连接信息 |
| `GET /sse/info` | 所有 Topic 信息 |

## 通配符规则

| 模式 | 匹配 | 不匹配 |
|------|------|--------|
| `stock.*` | `stock.AAPL`, `stock.GOOG` | `stock.AAPL.detail` |
| `user.#.events` | `user.123.events`, `user.123.profile.events` | `order.123.events` |
| `#` | 所有 Topic | — |

## 项目结构

```
cn.gmlee.tools.im/
├── annotation/
│   └── TopicSubscription.java        # @TopicSubscription 注解
├── core/
│   ├── TopicPublisher.java           # 发布者接口
│   ├── TopicSubscriber.java          # 订阅者接口
│   ├── TopicRouter.java              # 路由器接口
│   ├── DefaultTopicPublisher.java    # 发布者实现
│   └── DefaultTopicRouter.java       # 路由器实现（通配符匹配）
├── event/
│   └── TopicMessage.java             # 消息封装
├── sse/
│   ├── SseController.java            # 动态 SSE Controller
│   ├── SseConnectionManager.java     # SSE 连接管理
│   ├── SseTopicSubscriber.java       # SSE 桥接订阅者
│   └── SseProperties.java            # SSE 配置
├── stream/
│   ├── StreamBridgeBroadcaster.java  # SCS 广播发布器
│   ├── StreamBroadcastConsumer.java  # SCS 广播消费者
│   └── StreamProperties.java         # SCS 配置
└── config/
    └── SseImAutoConfiguration.java   # Spring Boot 自动配置
```

## 技术栈

- **Java 25** (最低 Java 17)
- **Spring Boot 3.x** (Spring Framework 6.x)
- **Spring WebFlux** (SSE 推送)
- **Spring Cloud Stream** (多实例消息同步，Broker 无关)
- **Lombok**

## 构建

```bash
export JAVA_HOME="C:/Java/jdk-25.0.3"
cd D:/tools
mvn clean install -DskipTests -pl tools-im
```
