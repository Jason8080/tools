package cn.gmlee.tools.agent.conf;

import cn.gmlee.tools.agent.bytebuddy.ByteBuddyAdvice;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * 方法超时监控自动装配.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ByteBuddyAutoConfiguration {

    private final ApplicationContext applicationContext;
    private final @Autowired(required = false) MonitorMethodProperties monitorMethodProperties;

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
        List<String> packages = NullUtil.get(monitorMethodProperties, MonitorMethodProperties::new).getPackages();
        for (String pack : packages) {
            emj = emj.or(ElementMatchers.nameStartsWith(pack));
        }
        return emj;
    }

    private ElementMatcher<? super TypeDescription> ignore() {
        ElementMatcher.Junction<NamedElement> emj = ElementMatchers.nameStartsWith("net.bytebuddy.");
        List<String> packages = NullUtil.get(monitorMethodProperties, MonitorMethodProperties::new).getIgnorePackages();
        for (String pack : packages) {
            emj = emj.or(ElementMatchers.nameStartsWith(pack));
        }
        return emj;
    }
}
