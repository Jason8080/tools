package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.BoolUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BoolTests {

    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(3000, 2000, 1000).stream().sorted().collect(Collectors.toList());
        System.out.println(list);
    }
}
