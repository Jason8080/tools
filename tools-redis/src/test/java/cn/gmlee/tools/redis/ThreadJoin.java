package cn.gmlee.tools.redis;

/**
 * 测试线程超时控制
 *
 * @author Jas°
 * @date 2020/12/23 (周三)
 */
public class ThreadJoin {

    public static void main(String[] args) throws InterruptedException  {
        Thread thread = new Thread(() -> {
            System.out.println(Thread.currentThread().getId()+": start");
            boolean flag = true;
            while (flag){
            }
            System.out.println(Thread.currentThread().getId()+": end");
        });
        execute(thread);
    }

    private static void execute(Thread thread) throws InterruptedException  {
        thread.start();
        // 堵塞运行: 指定时间
        thread.join(500);
        // 时间用完: 中断线程
        thread.stop();
        System.out.println(Thread.currentThread().getId()+": over~");
    }
}
