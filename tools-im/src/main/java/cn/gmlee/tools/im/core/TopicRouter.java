package cn.gmlee.tools.im.core;

import java.io.Serializable;
import java.util.List;

/**
 * The interface Topic route.
 */
public interface TopicRouter extends Serializable {
    /**
     * Route topic.
     *
     * @param <T>    the type parameter
     * @param topic  the topic
     * @param topics the topics
     * @return the topic
     */
    default <T extends Topic> T route(String topic, List<T> topics) {
        for (T t : topics) {
            if (String.valueOf(t.topic()).equalsIgnoreCase(topic)) {
                return t;
            }
        }
        throw new RuntimeException("不支持的 Topic: " + topic);
    }
}
