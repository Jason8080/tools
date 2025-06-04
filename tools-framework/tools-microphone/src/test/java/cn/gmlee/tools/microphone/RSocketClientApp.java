package cn.gmlee.tools.microphone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The type R socket client app.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RSocketClientApp.class)
public class RSocketClientApp {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(RSocketClientApp.class, args);
    }


    @Test
    public void test() {

    }
}
