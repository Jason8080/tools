package cn.gmlee.tools.im.ex;

/**
 * The type Topic not found exception.
 */
public class TopicNotFoundException extends RuntimeException {
    /**
     * Instantiates a new Topic not found exception.
     *
     * @param topic the topic
     */
    public TopicNotFoundException(String topic) {
        super("不支持的主题: " + topic);
    }
}
