package cn.gmlee.tools.base;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RexTests {
    public static void main(String[] args) {
        String text = "净重总和为：5净重总和为：54520 kg净重总和为：54520 kg";
        List<String> lastNumber = find(text, "(\\d+)(?!.*\\d)");
        System.out.println(lastNumber); // ["54520"]
    }

    public static List<String> find(String source, String regex) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }
}
