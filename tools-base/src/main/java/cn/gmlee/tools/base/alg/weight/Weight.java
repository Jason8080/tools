package cn.gmlee.tools.base.alg.weight;

import cn.gmlee.tools.base.util.AssertUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 灰度算法.
 */
public class Weight {
    private static final Set<Integer> temporary = new HashSet<>(100);
    private static final Random random = new Random();
    private static final int[][] empty = new int[0][0];


    /**
     * 随机生成灰度组.
     *
     * @param ratios the ratios
     * @return the 灰度组和比例
     */
    public synchronized static int[][] groups(int... ratios) {
        if (ratios == null || ratios.length == 0) {
            return empty;
        }
        // 保存灰度组
        int[][] groups = new int[ratios.length][];
        // 遍历灰度组
        int count = 0;
        for (int i = 0; i < ratios.length; i++) {
            int ratio = ratios[i];
            count += ratio;
            AssertUtil.lte(count, 100, String.format("权重超额分配(%s%% > 100%%): %s", count, Arrays.toString(ratios)));
            int[] group = random(ratio);
            groups[i] = group;
        }
        // 清除占用位
        temporary.clear();
        return groups;
    }

    /**
     * 是否允许请求.
     *
     * @param current 当前请求数
     * @param group   灰度位
     * @return the boolean
     */
    public static boolean request(long current, int... group) {
        long index = current % 100;
        if (group == null) {
            return false;
        }
        for (int i : group) {
            if (i == index) {
                return true;
            }
        }
        return false;
    }

    /**
     * 随机生成n个0~100以内的数字.
     *
     * @param n the n
     * @return the set
     */
    private synchronized static int[] random(int n) {
        AssertUtil.lte(n, 100, String.format("最大%s无法生成%s个随机值", 100, n));

        int[] arr = new int[n];

        for (int i = 0; i < n; ) {
            int num = random.nextInt(100);
            // 已经存在不需要
            if (Weight.temporary.contains(num)) {
                continue;
            }
            arr[i++] = num;
            Weight.temporary.add(num);
        }

        return arr;
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        int[][] groups = groups(10, 20, 30, 40);
        for (int[] group : groups) {
            System.out.println(Arrays.toString(group));
        }
    }
}
