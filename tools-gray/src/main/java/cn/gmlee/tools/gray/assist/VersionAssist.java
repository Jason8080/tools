package cn.gmlee.tools.gray.assist;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.gray.conf.GrayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Version assist.
 */
@Slf4j
public class VersionAssist {
    /**
     * Gets newest version.
     *
     * @param discoveryClient the discovery client
     * @param grayProperties  the gray properties
     * @param serviceId       the service id
     * @return the newest version
     */
    public static String getNewestVersion(DiscoveryClient discoveryClient, GrayProperties grayProperties, String serviceId) {
        List<Long> versions = getVersions(discoveryClient, grayProperties, serviceId);
        int index = versions.size() - 1;
        Long v = NullUtil.get(versions.get(index), 0L);
        if (v >= Long.MAX_VALUE) {
            return "0";
        }
        if (index == 0) {
            QuickUtil.isTrue(grayProperties.getLog(), () -> log.info("服务[{}]发布新版: {}", serviceId, v + 1));
            return String.valueOf(v + 1);
        }
        // 加入到最后1个版本集群
        QuickUtil.isTrue(grayProperties.getLog(), () -> log.info("服务[{}]加入集群: {}", serviceId, v));
        return v.toString();
    }

    /**
     * Gets versions.
     *
     * @param discoveryClient the discovery client
     * @param grayProperties  the gray properties
     * @param serviceId       the service id
     * @return the versions
     */
    public static List<Long> getVersions(DiscoveryClient discoveryClient, GrayProperties grayProperties, String serviceId) {
        if (discoveryClient == null) {
            return Arrays.asList(-1L);
        }
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        QuickUtil.isTrue(grayProperties.getLog(), () -> log.debug("服务[{}]发现实例: {}", serviceId, JsonUtil.format(instances)));
        if (BoolUtil.isEmpty(instances)) {
            return Arrays.asList(0L);
        }
        Map<String, List<ServiceInstance>> instanceMap = instances.stream().collect(Collectors.groupingBy(x -> x.getMetadata().get(grayProperties.getHead())));
        Stream<String> stream = instanceMap.keySet().stream().filter(BoolUtil::isDigit);
        List<String> versions = stream.collect(Collectors.toList());
        QuickUtil.isTrue(grayProperties.getLog(), () -> log.debug("服务[{}]已有版本: {}", serviceId, versions));
        if (versions.isEmpty()) {
            return Arrays.asList(0L);
        }
        List<Long> vs = versions.stream().distinct().map(Long::valueOf).sorted().collect(Collectors.toList());
        if (vs.isEmpty()) {
            vs.add(0L);
        }
        return vs;
    }
}
