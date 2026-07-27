package cn.gmlee.tools.im.demo;

import cn.gmlee.tools.im.core.TopicSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 股票价格订阅者示例 - 演示 TopicSubscriber 接口使用
 *
 * 订阅所有股票价格更新（使用通配符 *）
 */
@Slf4j
@Component
public class StockPriceLogger implements TopicSubscriber<StockPrice> {

    @Override
    public String getTopic() {
        // 订阅所有 stock.* Topic
        return "stock.*";
    }

    @Override
    public void onMessage(String topic, StockPrice price) {
        log.info("[Logger] {}: ${} (change: {})",
            price.getSymbol(),
            price.getPrice(),
            price.getChange());
    }

    @Override
    public String getSubscriberId() {
        return "StockPriceLogger";
    }
}
