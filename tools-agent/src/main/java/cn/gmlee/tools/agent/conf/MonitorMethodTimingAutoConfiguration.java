package cn.gmlee.tools.agent.conf;

import cn.gmlee.tools.agent.bytebuddy.TimeoutWatcher;
import cn.gmlee.tools.agent.bytebuddy.TimingAdvice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.lang.instrument.Instrumentation;

/**
 * 方法超时监控自动装配.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MonitorMethodProperties.class)
public class MonitorMethodTimingAutoConfiguration {

    private final ApplicationContext applicationContext;
    private final MonitorMethodProperties monitorMethodProperties;

    /**
     * 超时监控.
     *
     * @return the timeout watcher
     */
    @Bean
    public TimeoutWatcher timeoutWatcher() {
        return new TimeoutWatcher(applicationContext, monitorMethodProperties);
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        log.info("[Tools ByteBuddy] Timing Agent is initializing...");

        Instrumentation instrumentation = ByteBuddyAgent.install();
        if (instrumentation == null) {
            log.error("Failed to install ByteBuddy agent");
            return;
        }

        new AgentBuilder.Default()
                .ignore(ElementMatchers.nameStartsWith("net.bytebuddy.")
                        .or(ElementMatchers.nameStartsWith("cn.gmlee.tools.")))
                .type(ElementMatchers.nameStartsWith("com.ldw."))
//                        .and(ElementMatchers.not(ElementMatchers.nameContains("$$"))) // 不是JDK代理类
//                        .and(ElementMatchers.not(ElementMatchers.nameContains("CGLIB"))) // 不是CGLIB代理类
//                        .and(ElementMatchers.not(ElementMatchers.nameContains("EnhancerBySpring")))) // 不是SPRING代理类
                .transform((builder, typeDescription, classLoader, module) ->
                        builder.visit(Advice.to(TimingAdvice.class).on(ElementMatchers.isMethod()
                                .and(ElementMatchers.not(ElementMatchers.isConstructor())) // 不是构造方法
                                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("lambda$"))))))
                .installOn(instrumentation);

        log.info("[Tools ByteBuddy] Timing Agent installed.");
    }
}
