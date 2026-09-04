package cn.gmlee.tools.im.endpoint;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.model.MessageMap;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.ResumeSignal;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.resume.EventIdCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link EndpointRouter} 信封 → SSE 事件映射单元测试.
 * <p>
 * 覆盖断点续传（v5.7.0）线协议契约：{@code id:} 字段（可配置关闭）、
 * {@code event: resync} 命名事件（携带 data 载荷）、{@code retry:} 字段（至多一次）。
 * </p>
 *
 * @since 5.7.0
 */
@DisplayName("EndpointRouter 信封 → SSE 事件映射")
class EndpointRouterSseMappingTest {

    private static final String TOPIC = "im.chat";

    private SseProperties sseProperties;
    private EndpointRouter router;
    /** 模拟单次 PULL 请求的作用域（每个连接一个，跨事件共享） */
    private AtomicBoolean retrySent;

    @BeforeEach
    void setUp() {
        sseProperties = new SseProperties();
        router = new EndpointRouter(null, null, sseProperties, null);
        retrySent = new AtomicBoolean(false);
    }

    @Test
    @DisplayName("普通信封：data = 载荷（MessageMap 原样），id = 信封 ID")
    void normalEnvelopeMapsDataAndId() {
        MessageMap payload = new MessageMap(Map.of("content", "hi"));
        ServerSentEvent<MessageMap> sse = router.toServerSentEvent(env(42L, payload), retrySent);

        assertEquals("42", sse.id(), "信封 ID 应编码为 SSE id: 字段");
        assertNull(sse.event(), "普通消息不应携带事件名");
        assertSame(payload, sse.data(), "MessageMap 载荷应原样下发");
        assertNull(sse.retry(), "默认不下发 retry: 字段");
    }

    @Test
    @DisplayName("非 MessageMap 载荷：包装为 {\"data\": 载荷}")
    void nonMessageMapPayloadWrapped() {
        ServerSentEvent<MessageMap> sse = router.toServerSentEvent(env(1L, new PlainMsg("hello")), retrySent);

        assertEquals("1", sse.id());
        assertNotNull(sse.data());
        assertInstanceOf(PlainMsg.class, sse.data().get("data"));
    }

    @Test
    @DisplayName("emit-id=false：不下发 id 字段（客户端失去续传位点）")
    void emitIdDisabledSkipsIdField() {
        sseProperties.getResume().setEmitId(false);
        ServerSentEvent<MessageMap> sse = router.toServerSentEvent(env(42L, msg()), retrySent);

        assertNull(sse.id());
        assertNotNull(sse.data(), "载荷仍应正常下发");
    }

    @Test
    @DisplayName("信封 ID 为 null：不下发 id 字段")
    void nullEnvelopeIdSkipsIdField() {
        ServerSentEvent<MessageMap> sse = router.toServerSentEvent(env(null, msg()), retrySent);

        assertNull(sse.id());
        assertNotNull(sse.data());
    }

    @Test
    @DisplayName("ResumeSignal 信封：映射为 event: resync 命名事件（携带 data 载荷，无 id）")
    void resumeSignalMapsToNamedEvent() {
        ServerSentEvent<MessageMap> sse = router.toServerSentEvent(ResumeSignal.envelope(TOPIC), retrySent);

        assertEquals(ResumeSignal.EVENT_RESYNC, sse.event());
        assertNotNull(sse.data(), "命名事件必须携带 data 载荷，否则浏览器不派发");
        assertEquals(ResumeSignal.EVENT_RESYNC, sse.data().get("signal"));
        assertNull(sse.id(), "信号事件不应携带 id（浏览器需保留上一有效位点）");
    }

    @Test
    @DisplayName("retry-advice：仅随首个事件下发一次")
    void retryAdviceSentOnlyOnce() {
        sseProperties.getResume().setRetryAdvice(Duration.ofSeconds(3));

        ServerSentEvent<MessageMap> first = router.toServerSentEvent(env(1L, msg()), retrySent);
        ServerSentEvent<MessageMap> second = router.toServerSentEvent(env(2L, msg()), retrySent);

        assertEquals(Duration.ofSeconds(3), first.retry());
        assertNull(second.retry(), "retry: 字段在同一连接内至多一次");
    }

    @Test
    @DisplayName("未配置 retry-advice：任何事件都不下发 retry 字段")
    void noRetryAdviceByDefault() {
        ServerSentEvent<MessageMap> sse = router.toServerSentEvent(env(1L, msg()), retrySent);

        assertNull(sse.retry());
    }

    @Test
    @DisplayName("编码器抛异常：跳过 id 字段，事件仍正常构建（不断流）")
    void codecFailureSkipsIdWithoutBreakingEvent() {
        EventIdCodec broken = new EventIdCodec() {
            @Override
            public String encode(Serializable id) {
                throw new IllegalStateException("encode boom");
            }

            @Override
            public Serializable decode(String raw) {
                return raw;
            }
        };
        EndpointRouter brokenRouter = new EndpointRouter(null, null, sseProperties, null, null, null, broken);

        ServerSentEvent<MessageMap> sse = brokenRouter.toServerSentEvent(env(7L, msg()), retrySent);

        assertNull(sse.id(), "编码失败应跳过 id 字段");
        assertNotNull(sse.data(), "载荷不受编码失败影响");
    }

    // ==================== 测试基础设施 ====================

    private static TopicMessage<Serializable, Msg> env(Serializable id, Msg msg) {
        TopicMessage<Serializable, Msg> m = new TopicMessage<>();
        m.setId(id);
        m.setTopic(TOPIC);
        m.setMsg(msg);
        return m;
    }

    private static MessageMap msg() {
        return new MessageMap(Map.of("content", "hi"));
    }

    /** 非 MessageMap 的普通消息 */
    private static final class PlainMsg implements Msg {
        private final String text;

        PlainMsg(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "PlainMsg[" + text + "]";
        }
    }
}
