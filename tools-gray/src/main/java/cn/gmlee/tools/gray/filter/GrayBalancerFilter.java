package cn.gmlee.tools.gray.filter;

import cn.gmlee.tools.gray.balancer.GrayReactorServiceInstanceLoadBalancer;
import cn.gmlee.tools.gray.server.GrayServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalancerUriTools;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 灰度发布过滤器.
 */
@Slf4j
@SuppressWarnings("all")
public class GrayBalancerFilter implements GlobalFilter, Ordered {

    private static final int LOAD_BALANCER_CLIENT_FILTER_ORDER = 10149;
    private final LoadBalancerClientFactory clientFactory;
    private final GrayServer grayServer;

    /**
     * Instantiates a new Gray load balancer client filter.
     *
     * @param clientFactory the client factory
     * @param properties    the properties
     */
    public GrayBalancerFilter(LoadBalancerClientFactory clientFactory, GrayServer grayServer) {
        this.clientFactory = clientFactory;
        this.grayServer = grayServer;
    }

    @Override
    public int getOrder() {
        return LOAD_BALANCER_CLIENT_FILTER_ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return doFilter(exchange, chain);
    }

    private Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!grayServer.properties.getEnable()) {
            return chain.filter(exchange);
        }
        return this.choose(exchange).doOnNext((response) -> {
            if (!response.hasServer()) {
                String msg = String.format("实例丢失: %s", exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR));
                throw NotFoundException.create(true, msg);
            }
            URI uri = exchange.getRequest().getURI();
            DelegatingServiceInstance serviceInstance = new DelegatingServiceInstance(response.getServer(), uri.getScheme());
            URI requestUrl = this.reconstructURI(serviceInstance, uri);
            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, requestUrl);
        }).then(chain.filter(exchange));
    }

    /**
     * Reconstruct uri uri.
     *
     * @param serviceInstance the service instance
     * @param original        the original
     * @return the uri
     */
    protected URI reconstructURI(ServiceInstance serviceInstance, URI original) {
        return LoadBalancerUriTools.reconstructURI(serviceInstance, original);
    }

    private Mono<Response<ServiceInstance>> choose(ServerWebExchange exchange) {
        URI uri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        GrayReactorServiceInstanceLoadBalancer loadBalancer = new GrayReactorServiceInstanceLoadBalancer(
                clientFactory.getLazyProvider(uri.getHost(), ServiceInstanceListSupplier.class), grayServer
        );
        return loadBalancer.choose(new DefaultRequest(exchange));
    }
}