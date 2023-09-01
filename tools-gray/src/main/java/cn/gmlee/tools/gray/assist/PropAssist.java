package cn.gmlee.tools.gray.assist;

import cn.gmlee.tools.gray.conf.GrayProperties;
import cn.gmlee.tools.gray.mod.App;
import cn.gmlee.tools.gray.mod.Rule;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 配置提取工具.
 */
public class PropAssist {
    /**
     * Enable boolean.
     *
     * @param exchange   the exchange
     * @param properties the properties
     * @return the boolean
     */
    public static boolean enable(ServerWebExchange exchange, GrayProperties properties) {
        // 网关开关
        Boolean global = properties.getEnable();
        if (Boolean.FALSE.equals(global)) {
            return false;
        }
        // APP开关
        String serviceId = ExchangeAssist.getServiceId(exchange);
        App app = properties.getApps().get(serviceId);
        return app != null ? app.getEnable() : false;
    }

    /**
     * Gets versions.
     *
     * @param properties the properties
     * @param serviceId  the service id
     * @return the versions
     */
    public static List<String> getVersions(GrayProperties properties, String serviceId) {
        App app = properties.getApps().get(serviceId);
        return app != null ? app.getVersions() : Collections.emptyList();
    }

    /**
     * Gets rules.
     *
     * @param properties the properties
     * @param serviceId  the service id
     * @return the rules
     */
    public static Map<String, Rule> getRules(GrayProperties properties, String serviceId) {
        App app = properties.getApps().get(serviceId);
        return app != null ? app.getRules() : Collections.emptyMap();
    }
}
