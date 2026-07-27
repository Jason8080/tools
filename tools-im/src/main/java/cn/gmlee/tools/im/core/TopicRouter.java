package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.event.TopicMessage;

import java.util.Collection;
import java.util.List;

/**
 * Topic 路由器接口
 * <p>
 * 负责将消息路由到正确的订阅者
 * </p>
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
public interface TopicRouter {

    /**
     * 路由消息到所有匹配的订阅者
     *
     * @param message Topic 消息
     */
    void route(TopicMessage<?> message);

    /**
     * 路由消息到指定 Topic 的订阅者
     *
     * @param topic   Topic 名称
     * @param payload 消息负载
     */
    void route(String topic, Object payload);

    /**
     * 注册订阅者
     *
     * @param subscriber 订阅者
     */
    void registerSubscriber(TopicSubscriber<?> subscriber);

    /**
     * 注销订阅者
     *
     * @param subscriber 订阅者
     */
    void unregisterSubscriber(TopicSubscriber<?> subscriber);

    /**
     * 获取指定 Topic 的所有订阅者
     *
     * @param topic Topic 名称
     * @return 订阅者列表
     */
    List<TopicSubscriber<?>> getSubscribers(String topic);

    /**
     * 获取所有已注册的订阅者
     *
     * @return 订阅者集合
     */
    Collection<TopicSubscriber<?>> getAllSubscribers();

    /**
     * 获取所有已注册的 Topic
     *
     * @return Topic 集合
     */
    Collection<String> getAllTopics();

    /**
     * 检查 Topic 是否匹配模式
     *
     * @param topic   Topic 名称
     * @param pattern 模式（支持 * 和 #）
     * @return 是否匹配
     */
    boolean matches(String topic, String pattern);

    /**
     * 清空所有订阅者
     */
    void clear();
}
