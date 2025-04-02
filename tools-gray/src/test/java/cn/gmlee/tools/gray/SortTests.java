package cn.gmlee.tools.gray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SortTests {

    public static void main(String[] args) {
        Map map = new HashMap();
        map.put("2", "2");
        map.put("0", "0");
        map.put("1", "1");
        System.out.println(new TreeMap(map).lastEntry());
        System.out.println(Arrays.asList("2","0","1").stream().map(Long::valueOf).distinct().sorted().collect(Collectors.toList()));
    }
}
