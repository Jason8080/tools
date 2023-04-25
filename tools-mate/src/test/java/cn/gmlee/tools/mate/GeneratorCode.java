package cn.gmlee.tools.mate;

import cn.gmlee.tools.code.CodeGenerator;

/**
 * .
 *
 * @author Jas°
 * @date 2021/8/16 (周一)
 */
public class GeneratorCode extends CodeGenerator {



    public static void main(String[] args) {
        init("", "tools-mate", "",
                "cn.gmlee",
                "",
                "jdbc:mysql://dal-dev.gmlee.cn:9803/css_user_center?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai",
                "css_user_center_user",
                "Zu8v8FN40lWUicEoc34m"
        );

        execute();
    }
}
