package cn.gmlee.tools.agent.aop;

import cn.gmlee.tools.agent.conf.MonitorMethodProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public class AspectInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private static final String MODE = "AOP";

    private final ConfigurableApplicationContext context;
    private final MonitorMethodProperties props;

    public AspectInitializer(ConfigurableApplicationContext context,
                             MonitorMethodProperties props) {
        this.context = context;
        this.props = props;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if(props != null && MODE.equalsIgnoreCase(props.getMode())){
            log.info("[Tools AroundAspect] Timing Agent is initializing...");
            context.getBeanFactory().registerSingleton(
                    "aroundAspect",
                    new AroundAspect(props)
            );
            log.info("[Tools AroundAspect] Timing Agent is initialized.");
        }
    }
}
