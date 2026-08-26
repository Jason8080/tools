package cn.gmlee.tools.base.anno;

import java.lang.annotation.*;

/**
 * MCP 工具元信息注解 —— 标记类、方法、参数或字段为 MCP 工具元素，并提供描述信息。
 * <p>
 * <b>可用位置：</b>
 * <ul>
 *   <li>类级别：描述整个接口/类的用途</li>
 *   <li>方法级别：描述具体方法的用途</li>
 *   <li>参数字段级别：描述参数或字段的含义、约束等</li>
 * </ul>
 * </p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * // 1. 方法级
 * @FeignClient(name = "order-service")
 * @Mcp("订单服务")
 * public interface OrderFeignClient {
 *     @Mcp("创建采购订单")
 *     @PostMapping("/order/create")
 *     Result<OrderVO> createOrder(@RequestBody CreateOrderReq req);
 * }
 *
 * // 2. 字段级（DTO）
 * public class CreateOrderReq {
 *     @Mcp(value = "商品ID", required = true)
 *     private Long goodsId;
 *
 *     @Mcp(value = "购买数量", required = true, defaultValue = "1")
 *     private BigDecimal quantity;
 * }
 * }</pre>
 *
 * <p><b>扫描规则：</b></p>
 * <ul>
 *   <li>ai-mcp-registry 启动时自动扫描所有 Spring Bean</li>
 *   <li>类上有 @Mcp：该类所有 public 方法都会被注册为 tool</li>
 *   <li>方法上有 @Mcp：仅该方法被注册为 tool</li>
 *   <li>字段/参数上有 @Mcp：为该字段/参数生成描述信息，写入 JSON Schema</li>
 *   <li>类和方法都有 @Mcp：方法级优先</li>
 * </ul>
 *
 * @see com.ldw.ai.mcp.registry.McpToolDefinition 声明式工具定义（非侵入式方案）
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mcp {
    /**
     * 描述内容（LLM 看到的文本）
     * <p>
     * 如果为空，则使用默认描述："调用 类名.方法名" 或字段名
     * </p>
     */
    String value() default "";

    /**
     * 是否必填（仅对参数/字段有效）
     * <p>
     * 默认 true
     * </p>
     */
    boolean required() default true;

    /**
     * 默认值（仅对参数/字段有效）
     * <p>
     * 如果设置了默认值，会自动标记为 required = false
     * </p>
     */
    String defaultValue() default "";

    /**
     * 枚举值列表（仅对参数/字段有效）
     * <p>
     * 限制参数/字段的取值范围
     * </p>
     */
    String[] enumValues() default {};

    /**
     * 使用示例（仅对方法有效）
     * <p>
     * 帮助 LLM 理解如何调用该工具
     * </p>
     */
    String[] examples() default {};
}
