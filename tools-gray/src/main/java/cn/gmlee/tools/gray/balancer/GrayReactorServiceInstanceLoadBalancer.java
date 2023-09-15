package cn.gmlee.tools.gray.balancer;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.CollectionUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.gray.assist.ExchangeAssist;
import cn.gmlee.tools.gray.assist.HeaderAssist;
import cn.gmlee.tools.gray.assist.InstanceAssist;
import cn.gmlee.tools.gray.assist.PropAssist;
import cn.gmlee.tools.gray.server.GrayServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 灰度负载处理器.
 */
@Slf4j
public class GrayReactorServiceInstanceLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final AtomicInteger position = new AtomicInteger(new Random().nextInt(1000));

    private final LoadBalancerClientFactory factory;
    private final GrayServer grayServer;

    /**
     * Instantiates a new Gray reactor service instance load balancer.
     *
     * @param factory    the factory
     * @param grayServer the gray server
     */
    public GrayReactorServiceInstanceLoadBalancer(LoadBalancerClientFactory factory, GrayServer grayServer) {
        this.factory = factory;
        this.grayServer = grayServer;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        Object context = request.getContext();
        String serviceId = ExchangeAssist.getServiceId(context);
        if (PropAssist.enable(serviceId, grayServer.properties)) {
            ServiceInstanceListSupplier supplier = getServiceInstanceListSupplier(serviceId);
            return supplier.get(request).next().map(item -> filter(item, context));
        }
        log.debug("灰度服务: {} 开关检测: {} 全局开关: {}", serviceId, false, grayServer.properties.getEnable());
        ServiceInstanceListSupplier supplier = getServiceInstanceListSupplier(serviceId);
        return supplier.get(request).next().map(this::roll);
    }

    private ServiceInstanceListSupplier getServiceInstanceListSupplier(String serviceId) {
        ObjectProvider<ServiceInstanceListSupplier> suppliers = this.factory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class);
        return suppliers.getIfAvailable(NoopServiceInstanceListSupplier::new);
    }

    private Response<ServiceInstance> filter(List<ServiceInstance> all, Object exchange) {
        // 获取请求头部
        String serviceId = ExchangeAssist.getServiceId(exchange);
        HttpHeaders headers = ExchangeAssist.getHeaders(exchange);
        Response<ServiceInstance> response = getResponse(serviceId, headers, all);
        // 添加版本透传
        ServiceInstance instance = response.getServer();
        String head = grayServer.properties.getHead();
        ExchangeAssist.addHeader(exchange, head, instance.getMetadata().get(head));
        return response;
    }

    private Response<ServiceInstance> getResponse(String serviceId, HttpHeaders headers, List<ServiceInstance> all) {
        // 获取灰度实例
        List<ServiceInstance> gray = getGrayInstances(all, headers, serviceId);
        // 获取常规实例
        List<ServiceInstance> normal = getInstances(all, gray, headers, serviceId);
        // 返回可用实例
        return roll(normal, gray);
    }

    private List<ServiceInstance> getInstances(List<ServiceInstance> all, List<ServiceInstance> gray, HttpHeaders headers, String serviceId) {
        Map<String, String> tokens = ExchangeAssist.getTokens(headers, grayServer.properties.getToken());
        String version = HeaderAssist.getVersion(headers, grayServer.properties);
        boolean checked = grayServer.check(serviceId, tokens, version);
        log.debug("灰度服务: {} 检测结果: {} 全部实例: \r\n{}", serviceId, checked, JsonUtil.format(all));
        List<ServiceInstance> normal = exclude(all, gray);
        List<ServiceInstance> instances = checked ? gray : normal;
        log.debug("灰度服务: {} 进入灰度: {} 预选实例: \r\n{}", serviceId, BoolUtil.notEmpty(gray) && BoolUtil.eq(instances, gray), JsonUtil.format(instances));
        return instances.isEmpty() ? all : instances;
    }

    private List<ServiceInstance> exclude(List<ServiceInstance> instances, List<ServiceInstance> gray) {
        // 排除灰度节点
        return instances.stream().filter(x -> !gray.contains(x)).collect(Collectors.toList());
    }

    @SuppressWarnings("all")
    private List<ServiceInstance> getGrayInstances(List<ServiceInstance> instances, HttpHeaders headers, String serviceId) {
        // 归类候选版本
        Map<String, List<ServiceInstance>> candidateMap = instances.stream()
                .filter(x -> BoolUtil.notEmpty(InstanceAssist.version(x, grayServer.properties)))
                .collect(Collectors.groupingBy(x -> InstanceAssist.version(x, grayServer.properties)));
        // 开发指定版本
        List<String> developVs = PropAssist.getVersions(grayServer.properties, serviceId);
        if (BoolUtil.notEmpty(developVs)) {
            // 开发指定了灰度版本但没有这个版本实例; 则不属于灰度
            if(!BoolUtil.containKey(candidateMap, developVs.toArray(new String[0]))){
                return Collections.emptyList();
            }
            // 有开发指定的版本就进入灰度
            log.debug("灰度服务: {} 开发指定: {} 实例列表: \r\n{}", serviceId, PropAssist.getVersions(grayServer.properties, serviceId), JsonUtil.format(candidateMap));
            CollectionUtil.filter(candidateMap, (k, v) -> developVs.contains(k));
            return getNewest(candidateMap, serviceId);
        }
        // 外部指定版本
        String externalVersion = HeaderAssist.getVersion(headers, grayServer.properties);
        if (BoolUtil.notEmpty(externalVersion)) {
            List<ServiceInstance> list = candidateMap.get(externalVersion);
            log.debug("灰度服务: {} 外部指定: {} 实例列表: \r\n{}", serviceId, externalVersion, JsonUtil.format(list));
            if (BoolUtil.notEmpty(list)) {
                return list;
            }
            return Collections.emptyList();
        }
        // 使用最新版本
        return getNewest(candidateMap, serviceId);
    }

    private static List<ServiceInstance> getNewest(Map<String, List<ServiceInstance>> candidateMap, String serviceId) {
        TreeMap<String, List<ServiceInstance>> treeMap = CollectionUtil.keyReverseSort(candidateMap);
        Map.Entry<String, List<ServiceInstance>> newest = treeMap.firstEntry();
        if (newest == null) {
            return Collections.emptyList();
        }
        log.debug("灰度服务: {} 最新版本: {} 实例列表: \r\n{}", serviceId, newest.getKey(), JsonUtil.format(newest.getValue()));
        return newest.getValue();
    }

    private Response<ServiceInstance> roll(List<ServiceInstance> instances) {
        return roll(instances, null);
    }

    /**
     * 轮训机制
     * {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer}
     */
    private Response<ServiceInstance> roll(List<ServiceInstance> instances, List<ServiceInstance> gray) {
        if (instances.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, null);
        }
        int pos = Math.abs(incrementAndGet());
        ServiceInstance instance = instances.get(pos % instances.size());
        log.info("灰度服务: {} 进入灰度: {} 命中实例: {}", instance.getServiceId(), BoolUtil.containOne(gray, instance), JsonUtil.format(instance));
        return new DefaultResponse(instance);
    }

    /**
     * 处理越界安全问题
     */
    private synchronized int incrementAndGet() {
        int current = this.position.get();
        if (current >= Integer.MAX_VALUE - 1) {
            this.position.set(0);
        }
        return this.position.incrementAndGet();
    }
}
