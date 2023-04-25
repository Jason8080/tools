package cn.gmlee.tools.redis;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 测试线程终止获取返回值是否会阻塞
 *
 * @author Jas°
 * @date 2020/12/24 (周四)
 */
public class TaskTests {

    public static <V> V task(Callable<V> call) throws Exception {
        FutureTask<V> task = new FutureTask(call);
        Thread thread = new Thread(task);
        thread.start();
        try {
            return task.get(1000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return null;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            thread.stop();
        }
    }

    public static void main(String[] args) throws Exception {
        String a = task(() -> {
            System.out.println();
            boolean flag = true;
            return "a666";
        });
        System.out.println(a);
    }
}
