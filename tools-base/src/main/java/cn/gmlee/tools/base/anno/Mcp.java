package cn.gmlee.tools.base.anno;

import java.lang.annotation.*;

/**
 * MCP 工具元信息注解 —— 标记类或方法为 MCP 工具，并提供描述信息。
 * <p>
 * <b>可用位置：</b>
 * <ul>
 *   <li>类级别：描述整个接口/类的用途</li>
 *   <li>方法级别：描述具体方法的用途</li>
 * </ul>
 * </p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * @FeignClient(name = "order-service")
 * @Mcp("订单服务")
 * public interface OrderFeignClient {
 *     @Mcp("创建采购订单")
 *     @PostMapping("/order/create")
 *     Result<OrderVO> createOrder(@RequestBody CreateOrderReq req);
 *
 *     @Mcp("查询订单详情")
 *     @GetMapping("/order/{id}")
 *     Result<OrderVO> getOrder(@PathVariable("id") Long id);
 * }
 * }</pre>
 *
 * <p><b>扫描规则：</b></p>
 * <ul>
 *   <li>ai-mcp-registry 启动时自动扫描所有 Spring Bean</li>
 *   <li>类上有 @Mcp：该类所有 public 方法都会被注册为 tool</li>
 *   <li>方法上有 @Mcp：仅该方法被注册为 tool</li>
 *   <li>类和方法都有 @Mcp：方法级优先</li>
 * </ul>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mcp {
    /**
     * 工具描述（LLM 看到的文本）
     * <p>
     * 如果为空，则使用默认描述："调用 类名.方法名"
     * </p>
     */
    String value() default "";
}
