package cn.gmlee.tools.agent.conf;

import cn.gmlee.tools.agent.bytebuddy.ByteBuddyAdvice;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * 方法超时监控自动装配.
 */
@Slf4j
public class ByteBuddyAutoConfiguration {

    private static final String MODE = "ByteBuddy";

    private MonitorMethodProperties monitorMethodProperties;

    /**
     * Instantiates a new Byte buddy auto configuration.
     *
     * @param props the props
     */
    public ByteBuddyAutoConfiguration(@Autowired(required = false) MonitorMethodProperties props) {
        this.monitorMethodProperties = props;
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        if (monitorMethodProperties != null && MODE.equalsIgnoreCase(monitorMethodProperties.getMode())) {
            ExceptionUtil.sandbox(this::install);
        }
    }

    private void install() {
        if (!monitorMethodProperties.getEnable()) {
            log.info("[Tools ByteBuddy] Timing Agent is close...");
            return;
        }
        log.info("[Tools ByteBuddy] Timing Agent is initializing...");

        Instrumentation instrumentation = ByteBuddyAgent.install();
        if (instrumentation == null) {
            log.error("Failed to install ByteBuddy agent");
            return;
        }

        new AgentBuilder.Default().ignore(ignoreClasses()).type(type())
                .transform((builder, typeDescription, classLoader, module) ->
                        builder.visit(Advice.to(ByteBuddyAdvice.class).on(ignoreMethods())))
                .installOn(instrumentation);

        log.info("[Tools ByteBuddy] Timing Agent installed.");
    }

    private ElementMatcher<? super NamedElement> type() {
        if (BoolUtil.isEmpty(NullUtil.get(monitorMethodProperties, MonitorMethodProperties::new).getType())) {
            return ElementMatchers.any();
        }
        ElementMatcher.Junction<NamedElement> emj = ElementMatchers.nameStartsWith("net.bytebuddy.");
        List<String> packages = NullUtil.get(monitorMethodProperties, MonitorMethodProperties::new).getType();
        for (String pack : packages) {
            emj = emj.or(ElementMatchers.nameStartsWith(pack));
        }
        return emj;
    }

    private ElementMatcher<? super NamedElement> ignoreClasses() {
        ElementMatcher.Junction<NamedElement> emj = ElementMatchers.nameStartsWith("net.bytebuddy.")
                .or(ElementMatchers.nameContainsIgnoreCase("$"))
                .or(ElementMatchers.nameContainsIgnoreCase("lambda"))
                .or(ElementMatchers.nameContainsIgnoreCase("mapper"))
                .or(ElementMatchers.nameContainsIgnoreCase("agent."))
                .or(ElementMatchers.nameContainsIgnoreCase("javassist."))
                .or(ElementMatchers.nameContainsIgnoreCase("instrument."))
                .or(ElementMatchers.nameStartsWith("sun."))
                .or(ElementMatchers.nameStartsWith("jdk."))
                .or(ElementMatchers.nameStartsWith("java."))
                .or(ElementMatchers.nameStartsWith("javax."))
                .or(ElementMatchers.nameStartsWith("com.sun."))
                .or(ElementMatchers.nameStartsWith("sun.reflect."))
                .or(ElementMatchers.nameStartsWith("java.lang.invoke."))
                .or(ElementMatchers.nameStartsWith("cn.gmlee.tools.agent."));
        List<String> packages = NullUtil.get(monitorMethodProperties, MonitorMethodProperties::new).getIgnore();
        for (String pack : packages) {
            if (pack.contains("#")) {
                continue;
            }
            emj = emj.or(ElementMatchers.nameContainsIgnoreCase(pack));
        }
        return emj;
    }

    private ElementMatcher<? super MethodDescription> ignoreMethods() {
        ElementMatcher.Junction<MethodDescription> emj = ElementMatchers.isMethod()
                .and(ElementMatchers.not(ElementMatchers.nameContainsIgnoreCase("$")))
                .and(ElementMatchers.not(ElementMatchers.isNative()))
                .and(ElementMatchers.not(ElementMatchers.isBridge()))
                .and(ElementMatchers.not(ElementMatchers.isSynthetic()))
                .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                .and(ElementMatchers.not(ElementMatchers.isEquals()))
                .and(ElementMatchers.not(ElementMatchers.isHashCode()))
                .and(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)));
        List<String> packages = NullUtil.get(monitorMethodProperties, MonitorMethodProperties::new).getIgnore();
        for (String pack : packages) {
            if (!pack.contains("#")) {
                continue;
            }
            String className = pack.substring(0, pack.indexOf('#'));
            String methodName = pack.substring(pack.indexOf('#') + 1);
            if (BoolUtil.isEmpty(className)) {
                emj.and(ElementMatchers.not(ElementMatchers.nameContainsIgnoreCase(methodName)));
                continue;
            }
            emj = emj.and(ElementMatchers.not(ElementMatchers.named(methodName).and(ElementMatchers.isDeclaredBy(ElementMatchers.named(className)))));
        }
        return emj;
    }
}
