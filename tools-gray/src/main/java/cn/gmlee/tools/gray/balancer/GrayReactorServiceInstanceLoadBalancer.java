package cn.gmlee.tools.gray.balancer;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.CollectionUtil;
import cn.gmlee.tools.gray.assist.InstanceAssist;
import cn.gmlee.tools.gray.server.GrayServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
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
@SuppressWarnings("all")
public class GrayReactorServiceInstanceLoadBalancer extends RoundRobinLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final AtomicInteger position = new AtomicInteger(new Random().nextInt(1000));

    private final String serviceId;
    private final ObjectProvider<ServiceInstanceListSupplier> supplier;
    private final GrayServer grayServer;

    /**
     * Instantiates a new Gray reactor service instance load balancer.
     *
     * @param serviceId  the service id
     * @param supplier   the supplier
     * @param properties the properties
     */
    public GrayReactorServiceInstanceLoadBalancer(String serviceId, ObjectProvider<ServiceInstanceListSupplier> supplier, GrayServer grayServer) {
        super(supplier, serviceId);
        this.serviceId = serviceId;
        this.supplier = supplier;
        this.grayServer = grayServer;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        if (!grayServer.properties.getEnable()) {
            return super.choose(request);
        }
        ServiceInstanceListSupplier supplier = this.supplier.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get().next().map(item -> getInstanceResponse(item, (HttpHeaders) request.getContext()));
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> all, HttpHeaders headers) {
        if (all.isEmpty()) {
            return new EmptyResponse();
        }
        // 获取灰度实例
        List<ServiceInstance> grayInstances = getGrayInstances(all, headers);
        // 返回可用实例
        return getInstanceResponse(grayServer.check(headers) ? grayInstances : exclude(all, grayInstances));
    }

    private List<ServiceInstance> exclude(List<ServiceInstance> instances, List<ServiceInstance> grayInstances) {
        // 排除灰度节点
        return instances.stream().filter(x -> !grayInstances.contains(x)).collect(Collectors.toList());
    }

    private List<ServiceInstance> getGrayInstances(List<ServiceInstance> instances, HttpHeaders headers) {
        // 归类候选版本
        Map<String, List<ServiceInstance>> candidateMap = instances.stream()
                // 顺序1
                .filter(x -> BoolUtil.notEmpty(InstanceAssist.version(x, grayServer.properties)))
                // 顺序2
                .filter(x -> InstanceAssist.matching(x, grayServer.properties))
                .collect(Collectors.groupingBy(x -> InstanceAssist.version(x, grayServer.properties)));
        // 使用指定版本
        List<String> heads = headers.get(grayServer.properties.getHead());
        if (BoolUtil.notEmpty(heads)) {
            String version = heads.get(0);
            List<ServiceInstance> list = candidateMap.get(version);
            if(BoolUtil.notEmpty(list)){
                log.info("灰度服务:{} 指定版本:{} 实例列表: {}", serviceId, version, list);
                return list;
            }
        }
        // 使用最新版本
        TreeMap<String, List<ServiceInstance>> treeMap = CollectionUtil.keyReverseSort(candidateMap);
        Map.Entry<String, List<ServiceInstance>> newest = treeMap.firstEntry();
        log.info("灰度服务:{} 最新版本:{} 实例列表: {}", serviceId, newest.getKey(), newest.getValue());
        return newest.getValue();
    }


    /**
     * 详情请查看链接
     * {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer#getInstanceResponse}
     *
     * @param instances
     * @return
     */
    private Response<ServiceInstance> getInstanceResponse(
            List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }

        int pos = Math.abs(this.position.incrementAndGet());

        ServiceInstance instance = instances.get(pos % instances.size());

        return new DefaultResponse(instance);
    }
}
