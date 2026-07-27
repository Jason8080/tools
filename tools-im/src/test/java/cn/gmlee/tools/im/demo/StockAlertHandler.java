package cn.gmlee.tools.im.demo;

import cn.gmlee.tools.im.annotation.TopicSubscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 注解方式订阅者示例 - 演示 @TopicSubscription 注解使用
 */
@Slf4j
@Component
public class StockAlertHandler {

    /**
     * 当价格变化超过阈值时发出告警
     */
    @TopicSubscription("stock.*")
    public void handlePriceAlert(String topic, StockPrice price) {
        double absChange = Math.abs(price.getChange().doubleValue());
        if (absChange > 0.5) {
            log.warn("[ALERT] {} price changed significantly: ${} (change: {})",
                price.getSymbol(),
                price.getPrice(),
                price.getChange());
        }
    }

    /**
     * 订阅汇总 Topic
     */
    @TopicSubscription("stock.all")
    public void handleAllStocks(StockPrice price) {
        log.debug("[Summary] {} = ${}", price.getSymbol(), price.getPrice());
    }
}
