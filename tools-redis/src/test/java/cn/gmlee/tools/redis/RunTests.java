package cn.gmlee.tools.redis;

/**
 * 测试存储过程是否独立线程
 *
 * @author Jas°
 * @date 2020/12/23 (周三)
 */
public class RunTests {

    public static void run(Runnable run) {
        run.run();
    }

    public static void main(String[] args) {
        run(() -> {
            System.out.println("start   "+System.currentTimeMillis());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        });
        System.out.println("end     "+System.currentTimeMillis());
    }
}
