package cn.gmlee.tools.agent.conf;

import cn.gmlee.tools.agent.bytebuddy.ByteBuddyAdvice;
import cn.gmlee.tools.agent.trigger.ByteBuddyTrigger;
import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.agent.trigger.TimeoutTrigger;
import cn.gmlee.tools.agent.watcher.TimeoutWatcher;
import cn.gmlee.tools.base.util.ExceptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;

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
    @ConditionalOnMissingBean(TimeoutWatcher.class)
    public TimeoutWatcher timeoutWatcher() {
        return new TimeoutWatcher(applicationContext, monitorMethodProperties);
    }

    /**
     * Byte buddy trigger byte buddy trigger.
     *
     * @return the byte buddy trigger
     */
    @Bean
    @ConditionalOnMissingBean(ByteBuddyTrigger.class)
    public ByteBuddyTrigger byteBuddyTrigger() {
        return new ByteBuddyTrigger() {
            @Override
            public void enter(Watcher watcher) {

            }

            @Override
            public void exit(Watcher watcher) {

            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(ByteBuddyTrigger.class)
    public TimeoutTrigger timeoutTrigger() {
        return new TimeoutTrigger() {
            @Override
            public void handle(Watcher watcher, long elapsed, long timout) {
                log.warn("\r\n-------------------- Tools Watcher --------------------\r\n[{}] {}ms\r\n{}#{}({})",
                        watcher.getThread().getName(),
                        watcher.elapsedMillis(),
                        watcher.getOriginalObj().getClass().getName(),
                        watcher.getOriginalMethod().getName(),
                        Arrays.toString(watcher.getArgs())
                );
            }
        };
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        ExceptionUtil.sandbox(this::install);
    }

    private void install() {
        log.info("[Tools ByteBuddy] Timing Agent is initializing...");

        Instrumentation instrumentation = ByteBuddyAgent.install();
        if (instrumentation == null) {
            log.error("Failed to install ByteBuddy agent");
            return;
        }

        new AgentBuilder.Default().ignore(ignore()).type(type())
                .transform((builder, typeDescription, classLoader, module) ->
                        builder.visit(Advice.to(ByteBuddyAdvice.class).on(ElementMatchers.isMethod()
                                .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("lambda$"))))))
                .installOn(instrumentation);

        log.info("[Tools ByteBuddy] Timing Agent installed.");
    }

    private ElementMatcher<? super TypeDescription> type() {
        ElementMatcher.Junction<NamedElement> emj = ElementMatchers.nameStartsWith("net.bytebuddy.");
        List<String> packages = monitorMethodProperties.getPackages();
        for (String pack : packages) {
            emj = emj.or(ElementMatchers.nameStartsWith(pack));
        }
        return emj;
    }

    private ElementMatcher<? super TypeDescription> ignore() {
        ElementMatcher.Junction<NamedElement> emj = ElementMatchers.nameStartsWith("net.bytebuddy.");
        List<String> packages = monitorMethodProperties.getIgnorePackages();
        for (String pack : packages) {
            emj = emj.or(ElementMatchers.nameStartsWith(pack));
        }
        return emj;
    }
}
