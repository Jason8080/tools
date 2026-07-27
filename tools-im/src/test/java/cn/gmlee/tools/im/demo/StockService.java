package cn.gmlee.tools.im.demo;

import cn.gmlee.tools.im.core.TopicPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * 股票服务示例 - 演示 TopicPublisher 使用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final TopicPublisher publisher;
    private final Random random = new Random();

    private BigDecimal aaplPrice = BigDecimal.valueOf(150.0);
    private BigDecimal googPrice = BigDecimal.valueOf(2800.0);
    private BigDecimal msftPrice = BigDecimal.valueOf(300.0);

    /**
     * 每秒更新股票价格
     */
    @Scheduled(fixedRate = 1000)
    public void updatePrices() {
        // 更新 AAPL
        aaplPrice = updatePrice(aaplPrice);
        StockPrice aapl = new StockPrice("AAPL", aaplPrice,
            BigDecimal.valueOf(random.nextDouble() * 2 - 1).setScale(2, RoundingMode.HALF_UP),
            LocalDateTime.now());

        // 发布到 "stock.AAPL" Topic
        publisher.publish("stock.AAPL", aapl);
        log.debug("Published AAPL price: {}", aapl.getPrice());

        // 更新 GOOG
        googPrice = updatePrice(googPrice);
        StockPrice goog = new StockPrice("GOOG", googPrice,
            BigDecimal.valueOf(random.nextDouble() * 10 - 5).setScale(2, RoundingMode.HALF_UP),
            LocalDateTime.now());
        publisher.publish("stock.GOOG", goog);

        // 更新 MSFT
        msftPrice = updatePrice(msftPrice);
        StockPrice msft = new StockPrice("MSFT", msftPrice,
            BigDecimal.valueOf(random.nextDouble() * 5 - 2.5).setScale(2, RoundingMode.HALF_UP),
            LocalDateTime.now());
        publisher.publish("stock.MSFT", msft);

        // 同时发布到汇总 Topic
        publisher.publish("stock.all", aapl);
        publisher.publish("stock.all", goog);
        publisher.publish("stock.all", msft);
    }

    private BigDecimal updatePrice(BigDecimal current) {
        double change = (random.nextDouble() - 0.5) * 0.02; // ±1%
        return current.multiply(BigDecimal.valueOf(1 + change)).setScale(2, RoundingMode.HALF_UP);
    }
}
