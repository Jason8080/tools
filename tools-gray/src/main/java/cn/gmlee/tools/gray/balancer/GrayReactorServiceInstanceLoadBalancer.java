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
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
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

    private final ObjectProvider<ServiceInstanceListSupplier> supplier;
    private final GrayServer grayServer;

    /**
     * Instantiates a new Gray reactor service instance load balancer.
     *
     * @param supplier   the supplier
     * @param grayServer the gray server
     */
    public GrayReactorServiceInstanceLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> supplier, GrayServer grayServer) {
        this.supplier = supplier;
        this.grayServer = grayServer;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = this.supplier.getIfAvailable(NoopServiceInstanceListSupplier::new);
        Object context = request.getContext();
        if (context instanceof ServerWebExchange) {
            // 此开关控制灰度负载均衡是否生效
            String serviceId = ExchangeAssist.getServiceId((ServerWebExchange) context);
            if (PropAssist.enable(serviceId, grayServer.properties)) {
                return supplier.get().next().map(item -> getResponse(item, (ServerWebExchange) context));
            }
        }
        if (context instanceof RequestDataContext) {
            String serviceId = ExchangeAssist.getServiceId((RequestDataContext) context);
            if (PropAssist.enable(serviceId, grayServer.properties)) {
                return supplier.get().next().map(item -> getResponse(item, (RequestDataContext) context));
            }
        }
        log.info("灰度负载拦截器有开关尚未开启; 总开关: {}", grayServer.properties.getEnable());
        return supplier.get().next().map(this::roundRobin);
    }

    private Response<ServiceInstance> getResponse(List<ServiceInstance> all, RequestDataContext exchange) {
        // 获取请求头部
        String serviceId = exchange.getClientRequest().getUrl().getHost();
        HttpHeaders headers = exchange.getClientRequest().getHeaders();
        // 获取灰度实例
        List<ServiceInstance> gray = getGrayInstances(all, headers, serviceId);
        // 返回可用实例
        Response<ServiceInstance> response = roundRobin(getInstances(all, gray, headers, serviceId));
        // 添加版本透传
        ServiceInstance instance = response.getServer();
        String head = grayServer.properties.getHead();
        exchange.getClientRequest().getHeaders().add(head, instance.getMetadata().get(head));
        return response;
    }

    private Response<ServiceInstance> getResponse(List<ServiceInstance> all, ServerWebExchange exchange) {
        // 获取请求头部
        HttpHeaders headers = ExchangeAssist.getHeaders(exchange);
        String serviceId = ExchangeAssist.getServiceId(exchange);
        // 获取灰度实例
        List<ServiceInstance> gray = getGrayInstances(all, headers, serviceId);
        // 返回可用实例
        Response<ServiceInstance> response = roundRobin(getInstances(all, gray, headers, serviceId));
        // 添加版本透传
        ServiceInstance instance = response.getServer();
        String head = grayServer.properties.getHead();
        exchange.getRequest().mutate().header(head, instance.getMetadata().get(head)).build();
        return response;
    }

    private List<ServiceInstance> getInstances(List<ServiceInstance> all, List<ServiceInstance> gray, HttpHeaders headers, String serviceId) {
        Map<String, String> tokens = ExchangeAssist.getTokens(headers, grayServer.properties.getToken());
        String version = HeaderAssist.getVersion(headers, grayServer.properties);
        boolean checked = grayServer.check(serviceId, tokens, version);
        log.debug("灰度服务: {} 检测结果: {} 全部实例: \r\n{}", serviceId, checked, JsonUtil.format(all));
        List<ServiceInstance> normal = exclude(all, gray);
        List<ServiceInstance> instances = checked ? gray : normal;
        log.info("灰度服务: {} 进入灰度: {} 预选实例: \r\n{}", serviceId, BoolUtil.notEmpty(gray) && BoolUtil.eq(instances, gray), JsonUtil.format(instances));
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
                // 顺序1: 版本号不可为空
                .filter(x -> BoolUtil.notEmpty(InstanceAssist.version(x, grayServer.properties)))
                // 顺序2: 开发指定的版本 √√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√
                .filter(x -> InstanceAssist.matching(x, grayServer.properties))
                .collect(Collectors.groupingBy(x -> InstanceAssist.version(x, grayServer.properties)));
        // 开发指定版本
        if (BoolUtil.notEmpty(PropAssist.getVersions(grayServer.properties, serviceId))) {
            log.debug("灰度服务: {} 开发指定: {} 实例列表: \r\n{}", serviceId, PropAssist.getVersions(grayServer.properties, serviceId), JsonUtil.format(candidateMap));
        }
        // 外部指定版本
        String version = HeaderAssist.getVersion(headers, grayServer.properties);
        if (BoolUtil.notEmpty(version)) {
            List<ServiceInstance> list = candidateMap.get(version);
            log.info("灰度服务: {} 外部指定: {} 实例列表: \r\n{}", serviceId, version, JsonUtil.format(list));
            if (BoolUtil.notEmpty(list)) {
                return list;
            }
            return Collections.emptyList();
        }
        // 使用最新版本
        TreeMap<String, List<ServiceInstance>> treeMap = CollectionUtil.keyReverseSort(candidateMap);
        Map.Entry<String, List<ServiceInstance>> newest = treeMap.firstEntry();
        if (newest == null) {
            return Collections.emptyList();
        }
        log.info("灰度服务: {} 最新版本: {} 实例列表: \r\n{}", serviceId, newest.getKey(), JsonUtil.format(newest.getValue()));
        return newest.getValue();
    }


    /**
     * 轮训机制
     * {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer}
     */
    private Response<ServiceInstance> roundRobin(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            throw NotFoundException.create(true, null);
        }
        int pos = Math.abs(incrementAndGet());
        ServiceInstance instance = instances.get(pos % instances.size());
        String serverPort = String.format("%s:%s", instance.getHost(), instance.getPort());
        log.info("灰度服务: {} 命中实例: {} ", instance.getServiceId(), serverPort);
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
