package cn.gmlee.tools.im.demo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 股票价格示例
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockPrice {
    private String symbol;
    private BigDecimal price;
    private BigDecimal change;
    private LocalDateTime timestamp;
}
