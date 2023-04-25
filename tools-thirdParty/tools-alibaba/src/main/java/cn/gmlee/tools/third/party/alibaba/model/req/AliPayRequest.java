package cn.gmlee.tools.third.party.alibaba.model.req;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Jas°
 * @date 2021/3/4 (周四)
 */
@Data
public class AliPayRequest implements Serializable {
    @NotNull(message = "付款金额是空")
    @Min(value = 1, message = "金额小于1分")
    private Long money;

    /**
     * 自定义参数: json
     */
    private String params = "";

    @NotEmpty(message = "外部订单号是空")
    private String outTradeNo;

    @NotEmpty(message = "商品描述是空 (格式: 平台名-商品名)")
    private String body;

    @NotEmpty(message = "订单名称是空")
    private String subject;
}
