package cn.gmlee.tools.im.annotation;

import java.lang.annotation.*;

/**
 * Topic 订阅注解
 * <p>
 * 用于标记方法为 Topic 消息处理器
 * </p>
 *
 * <pre>
 * // 精确订阅
 * &#64;TopicSubscription("stock.AAPL")
 * public void handleAapl(StockPrice price) { }
 *
 * // 通配符订阅（* 匹配单层）
 * &#64;TopicSubscription("stock.*")
 * public void handleAllStocks(String topic, StockPrice price) { }
 *
 * // 多层通配符（# 匹配多层）
 * &#64;TopicSubscription("user.#.events")
 * public void handleUserEvents(String topic, UserEvent event) { }
 * </pre>
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TopicSubscription {

    /**
     * Topic 名称（精确匹配）
     */
    String value() default "";

    /**
     * Topic 模式（支持通配符 * 和 #）
     */
    String pattern() default "";

    /**
     * 处理优先级（数值越大优先级越高）
     */
    int priority() default 0;

    /**
     * 是否异步处理
     */
    boolean async() default false;
}
