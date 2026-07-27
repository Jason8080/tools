package cn.gmlee.tools.im.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SseIm 框架演示应用
 *
 * 启动后访问：
 * - SSE 订阅 AAPL: http://localhost:8080/sse/stock.AAPL
 * - SSE 订阅所有股票: http://localhost:8080/sse/stock.*
 * - SSE 订阅汇总: http://localhost:8080/sse/stock.all
 */
@SpringBootApplication
@EnableScheduling
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
