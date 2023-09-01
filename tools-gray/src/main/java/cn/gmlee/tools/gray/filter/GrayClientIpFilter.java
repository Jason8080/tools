package cn.gmlee.tools.gray.filter;

import cn.gmlee.tools.base.util.WebUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 客户端IP.
 */
public class GrayClientIpFilter implements GlobalFilter {
 
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                //将获取的真实ip存入header微服务方便获取
                .header(WebUtil.REMOTE_ADDRESS_HOST, exchange.getRequest().getRemoteAddress().getHostString())
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }
 
}