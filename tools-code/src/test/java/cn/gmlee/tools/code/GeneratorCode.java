package cn.gmlee.tools.code;

/**
 * 代码生成器.
 *
 * @author Jas °
 */
public class GeneratorCode extends CodeGenerator {

    public static void main(String[] args) {
        init(
                "", "", "tools-code",
                "cn.gmlee", "",
                "jdbc:mysql://localhost:3306/db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai",
                "root", "root"
        );
        execute();
    }
}
