package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.event.TopicMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Topic 路由器默认实现
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
@Slf4j
public class DefaultTopicRouter implements TopicRouter {

    /**
     * 订阅者注册表：pattern -> subscribers
     */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<TopicSubscriber<?>>> subscribers = new ConcurrentHashMap<>();

    /**
     * 模式编译缓存：pattern -> compiled regex
     */
    private final ConcurrentHashMap<String, Pattern> patternCache = new ConcurrentHashMap<>();

    @Override
    public void route(TopicMessage<?> message) {
        if (message == null || message.getTopic() == null) {
            log.warn("Cannot route null message or message with null topic");
            return;
        }

        String topic = message.getTopic();
        List<TopicSubscriber<?>> matchedSubscribers = getSubscribers(topic);

        if (matchedSubscribers.isEmpty()) {
            log.debug("No subscribers found for topic: {}", topic);
            return;
        }

        // 按优先级排序
        matchedSubscribers.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));

        for (TopicSubscriber<?> subscriber : matchedSubscribers) {
            dispatchToSubscriber(subscriber, message);
        }
    }

    @Override
    public void route(String topic, Object payload) {
        TopicMessage<Object> message = TopicMessage.of(topic, payload);
        route(message);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void dispatchToSubscriber(TopicSubscriber subscriber, TopicMessage<?> message) {
        Executor executor = subscriber.getExecutor();

        Runnable task = () -> {
            try {
                subscriber.onTopicMessage(message);
            } catch (Exception e) {
                log.error("Error dispatching message to subscriber {} for topic {}",
                         subscriber.getSubscriberId(), message.getTopic(), e);
                try {
                    subscriber.onError(message.getTopic(), e);
                } catch (Exception ex) {
                    log.error("Error in onError handler for subscriber {}",
                             subscriber.getSubscriberId(), ex);
                }
            }
        };

        if (executor != null) {
            executor.execute(task);
        } else {
            task.run();
        }
    }

    @Override
    public void registerSubscriber(TopicSubscriber<?> subscriber) {
        String pattern = subscriber.getTopic();
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Topic pattern cannot be null or empty");
        }

        subscribers.computeIfAbsent(pattern, k -> new CopyOnWriteArrayList<>()).add(subscriber);
        log.info("Registered subscriber {} for topic pattern: {}",
                subscriber.getSubscriberId(), pattern);
    }

    @Override
    public void unregisterSubscriber(TopicSubscriber<?> subscriber) {
        String pattern = subscriber.getTopic();
        CopyOnWriteArrayList<TopicSubscriber<?>> list = subscribers.get(pattern);
        if (list != null) {
            list.remove(subscriber);
            if (list.isEmpty()) {
                subscribers.remove(pattern);
            }
            log.info("Unregistered subscriber {} from topic pattern: {}",
                    subscriber.getSubscriberId(), pattern);
        }
    }

    @Override
    public List<TopicSubscriber<?>> getSubscribers(String topic) {
        List<TopicSubscriber<?>> result = new ArrayList<>();

        for (Map.Entry<String, CopyOnWriteArrayList<TopicSubscriber<?>>> entry : subscribers.entrySet()) {
            String pattern = entry.getKey();
            if (matches(topic, pattern)) {
                result.addAll(entry.getValue());
            }
        }

        return result;
    }

    @Override
    public Collection<TopicSubscriber<?>> getAllSubscribers() {
        return subscribers.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getAllTopics() {
        return subscribers.keySet();
    }

    @Override
    public boolean matches(String topic, String pattern) {
        if (topic == null || pattern == null) {
            return false;
        }

        // 精确匹配
        if (topic.equals(pattern)) {
            return true;
        }

        // 检查是否包含通配符
        if (!pattern.contains("*") && !pattern.contains("#")) {
            return false;
        }

        // 编译并缓存正则表达式
        Pattern regex = patternCache.computeIfAbsent(pattern, this::compilePattern);
        return regex.matcher(topic).matches();
    }

    /**
     * 编译通配符模式为正则表达式
     * <ul>
     *   <li>* - 匹配单层（不包含点号）</li>
     *   <li># - 匹配多层（包含点号）</li>
     * </ul>
     */
    private Pattern compilePattern(String pattern) {
        StringBuilder regex = new StringBuilder("^");

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*') {
                regex.append("[^.]+");  // 匹配除点号外的任意字符
            } else if (c == '#') {
                regex.append(".*");  // 匹配任意字符（包括点号）
            } else if (c == '.') {
                regex.append("\\.");  // 转义点号
            } else {
                regex.append(c);
            }
        }

        regex.append("$");
        return Pattern.compile(regex.toString());
    }

    @Override
    public void clear() {
        subscribers.clear();
        patternCache.clear();
        log.info("Cleared all subscribers");
    }
}
