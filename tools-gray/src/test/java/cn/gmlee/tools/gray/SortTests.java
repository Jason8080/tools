package cn.gmlee.tools.gray;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SortTests {
    public static void main(String[] args) {
        System.out.println(Arrays.asList("1").stream().map(Long::valueOf).distinct().sorted().collect(Collectors.toList()));
    }
}
