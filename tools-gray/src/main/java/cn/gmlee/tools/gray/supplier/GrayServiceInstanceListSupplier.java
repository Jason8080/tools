package cn.gmlee.tools.gray.supplier;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.CollectionUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.gray.assist.ExchangeAssist;
import cn.gmlee.tools.gray.assist.HeaderAssist;
import cn.gmlee.tools.gray.assist.InstanceAssist;
import cn.gmlee.tools.gray.assist.PropAssist;
import cn.gmlee.tools.gray.server.GrayServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.loadbalancer.core.DelegatingServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The type Version service instance list supplier.
 */
@Slf4j
@SuppressWarnings("all")
public class GrayServiceInstanceListSupplier extends DelegatingServiceInstanceListSupplier {

    private final AtomicInteger position = new AtomicInteger(new Random().nextInt(1000));

    private final GrayServer grayServer;

    /**
     * Instantiates a new Gray service instance list supplier.
     *
     * @param delegate the delegate
     */
    public GrayServiceInstanceListSupplier(ServiceInstanceListSupplier delegate, GrayServer grayServer) {
        super(delegate);
        this.grayServer = grayServer;
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return delegate.get();
    }

    @Override
    public Flux<List<ServiceInstance>> get(Request request) {
        return delegate.get(request).map(instances -> filter(instances, request));
    }

    private List<ServiceInstance> filter(List<ServiceInstance> instances, Request request) {
        Object context = request.getContext();
        if(!(context instanceof RequestDataContext)){
            log.warn("灰度负载均衡器恢复轮询机制: {}", context.getClass());
            return instances;
        }
        String serviceId = ExchangeAssist.getServiceId((RequestDataContext) context);
        if (!PropAssist.enable(serviceId, grayServer.properties)) {
            log.info("灰度服务: {} 开关检测: {} 全局开关: {}", serviceId, false, grayServer.properties.getEnable());
            return instances;
        }
        return Arrays.asList(filter(instances, (RequestDataContext) context));
    }

    private ServiceInstance filter(List<ServiceInstance> all, RequestDataContext exchange) {
        // 获取请求头部
        String serviceId = exchange.getClientRequest().getUrl().getHost();
        HttpHeaders headers = exchange.getClientRequest().getHeaders();
        // 获取灰度实例
        List<ServiceInstance> gray = filterGray(all, headers, serviceId);
        // 返回可用实例
        List<ServiceInstance> usable = filterUsable(all, gray, headers, serviceId);
        // 轮询可用实例
        ServiceInstance instance = roll(usable);
        String head = grayServer.properties.getHead();
        exchange.getClientRequest().getHeaders().add(head, instance.getMetadata().get(head));
        return instance;
    }

    private List<ServiceInstance> filterGray(List<ServiceInstance> instances, HttpHeaders headers, String serviceId) {
        // 归类候选版本
        Map<String, List<ServiceInstance>> candidateMap = instances.stream()
                // 顺序1: 版本号不可为空
                .filter(x -> BoolUtil.notEmpty(InstanceAssist.version(x, grayServer.properties)))
                // 顺序2: 开发指定的版本 √√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√√
                .filter(x -> InstanceAssist.matching(x, grayServer.properties))
                .collect(Collectors.groupingBy(x -> InstanceAssist.version(x, grayServer.properties)));
        // 开发指定版本
        if (BoolUtil.notEmpty(PropAssist.getVersions(grayServer.properties, serviceId))) {
            log.info("灰度服务: {} 开发指定: {} 实例列表: \r\n{}", serviceId, PropAssist.getVersions(grayServer.properties, serviceId), JsonUtil.format(candidateMap));
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

    private List<ServiceInstance> filterUsable(List<ServiceInstance> all, List<ServiceInstance> gray, HttpHeaders headers, String serviceId) {
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


    /**
     * 轮训机制
     * {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer}
     */
    private ServiceInstance roll(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, null);
        }
        int pos = Math.abs(incrementAndGet());
        ServiceInstance instance = instances.get(pos % instances.size());
        String serverPort = String.format("%s:%s", instance.getHost(), instance.getPort());
        log.info("灰度服务: {} 命中实例: {} ", instance.getServiceId(), serverPort);
        return instance;
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
