package cn.gmlee.tools.gray.balancer;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.CollectionUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.gray.assist.ExchangeAssist;
import cn.gmlee.tools.gray.assist.InstanceAssist;
import cn.gmlee.tools.gray.server.GrayServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
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
        return supplier.get().next().map(item -> getResponse(item, (ServerWebExchange) request.getContext()));
    }

    private Response<ServiceInstance> getResponse(List<ServiceInstance> all, ServerWebExchange exchange) {
        if (!grayServer.properties.getEnable()) {
            return roundRobin(all);
        }
        // 获取灰度实例
        List<ServiceInstance> grayInstances = getGrayInstances(all, exchange);
        // 返回可用实例
        return roundRobin(getInstances(all, exchange, grayInstances));
    }

    private List<ServiceInstance> getInstances(List<ServiceInstance> all, ServerWebExchange exchange, List<ServiceInstance> gray) {
        boolean checked = grayServer.check(exchange);
        // 常规节点: 去除灰度节点后的节点称之为常规节点
        List<ServiceInstance> normal = exclude(all, gray);
        // 检测通过: 进入灰度节点
        // 检测常规: 进入常规节点
        List<ServiceInstance> instances = checked ? gray : normal;
        if(instances.isEmpty()){
            // 进入灰度发现没有灰度节点: 轮训全部节点
            // 进入常规发现都是灰度节点: 轮训所有灰度
            return checked ? all : gray;
        }
        return instances;
    }

    private List<ServiceInstance> exclude(List<ServiceInstance> instances, List<ServiceInstance> gray) {
        // 排除灰度节点
        return instances.stream().filter(x -> !gray.contains(x)).collect(Collectors.toList());
    }

    @SuppressWarnings("all")
    private List<ServiceInstance> getGrayInstances(List<ServiceInstance> instances, ServerWebExchange exchange) {
        // 获取请求头部
        HttpHeaders headers = ExchangeAssist.getHeaders(exchange);
        String serviceId = ExchangeAssist.getServiceId(exchange);
        // 归类候选版本
        Map<String, List<ServiceInstance>> candidateMap = instances.stream()
                // 顺序1: 版本号不可为空
                .filter(x -> BoolUtil.notEmpty(InstanceAssist.version(x, grayServer.properties)))
                // 顺序2: 开发指定的版本 √√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√
                .filter(x -> InstanceAssist.matching(x, grayServer.properties))
                .collect(Collectors.groupingBy(x -> InstanceAssist.version(x, grayServer.properties)));
        // 外部指定版本
        List<String> heads = headers.get(grayServer.properties.getHead());
        if (BoolUtil.notEmpty(heads)) {
            String version = heads.get(0);
            List<ServiceInstance> list = candidateMap.get(version);
            if (BoolUtil.notEmpty(list)) {
                log.info("灰度服务:{} 指定版本:{} 实例列表: \r\n{}", serviceId, version, JsonUtil.format(list));
                return list;
            }
        }
        // 使用最新版本
        TreeMap<String, List<ServiceInstance>> treeMap = CollectionUtil.keyReverseSort(candidateMap);
        Map.Entry<String, List<ServiceInstance>> newest = treeMap.firstEntry();
        log.info("灰度服务:{} 最新版本:{} 实例列表: \r\n{}", serviceId, newest.getKey(), JsonUtil.format(newest.getValue()));
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