package cn.gmlee.tools.im.definition;

/**
 * 主题定义接口（进-转-出 闭环聚合，单进程场景）.
 * <p>
 * 将一个完整 Topic 的三个组件（Publisher/Consumer/Subscriber）聚合为单一接口，
 * 提供零样板代码的 Topic 定义方式。
 * </p>
 * <p>
 * 继承 {@link PublisherDefinition} 和 {@link SubscriberDefinition}，
 * 同时具备发送端和拉取端的定义能力。
 * </p>
 *
 * <h3>适用场景</h3>
 * <ul>
 *   <li><b>单进程应用</b>：发送和拉取在同一进程中完成</li>
 *   <li><b>快速原型</b>：无需分离定义，快速搭建完整 Topic</li>
 * </ul>
 *
 * <h3>微服务场景</h3>
 * <p>
 * 如果发送消息和拉取消息是两个独立的微服务进程，应分别使用：
 * </p>
 * <ul>
 *   <li>发送端服务：实现 {@link PublisherDefinition}</li>
 *   <li>拉取端服务：实现 {@link SubscriberDefinition}</li>
 * </ul>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * @Component
 * public class OrderTopicDefinition implements TopicDefinition {
 *     @Override
 *     public String topic() {
 *         return "order.update";
 *     }
 *     // 使用默认实现，无需重写 createPublisher/createConsumer/createSubscriber
 * }
 * }</pre>
 *
 * <h3>自定义场景</h3>
 * <p>
 * 如需自定义消息构建逻辑或消费逻辑，可重写对应的 create 方法：
 * </p>
 * <pre>{@code
 * @Override
 * public Publisher<Serializable, Msg> createPublisher(StreamBridge streamBridge) {
 *     return new AbstractStreamPublisher(streamBridge) {
 *         @Override
 *         public String topic() { return OrderTopicDefinition.this.topic(); }
 *
 *         @Override
 *         public Serializable push(MultiValueMap<String, String> urlParams, Msg msg) {
 *             // 自定义发布逻辑
 *             return super.push(urlParams, msg);
 *         }
 *     };
 * }
 * }</pre>
 *
 * @see PublisherDefinition 发送端定义接口
 * @see SubscriberDefinition 拉取端定义接口
 */
public interface TopicDefinition extends PublisherDefinition, SubscriberDefinition {
    // 继承 PublisherDefinition.createPublisher()
    // 继承 SubscriberDefinition.createConsumer() 和 createSubscriber()
    // 无需定义任何新方法，零样板代码
}
