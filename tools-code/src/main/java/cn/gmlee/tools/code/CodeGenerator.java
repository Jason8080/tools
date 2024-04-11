package cn.gmlee.tools.code;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.*;

/**
 * 代码生成器.
 *
 * @author Jas °
 */
public class CodeGenerator {
    // -----------------------------------------------------------------------------------------------------------------

    private static String system = "";
    private static String model = "";
    private static String project = "";
    private static String rootPackage = "";
    private static String parentPackage = getParentPackage();

    private static String getParentPackage() {
        return rootPackage + getEmpty(system, ".") + getEmpty(model, ".") + getEmpty(project, ".");
    }

    private static String getEmpty(String empty, String symbol) {
        if ("".equals(empty)) {
            return "";
        }
        if(symbol.equals("/")){
            return symbol + empty;
        }
        return symbol + empty.replace("-", ".");
    }

    private static String tablePrefix = "t_";

    // -----------------------------------------------------------------------------------------------------------------

    private static String url = "jdbc:mysql://127.0.0.1:3306/db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
    private static String driverName = "com.mysql.cj.jdbc.Driver";
    private static String username = "root";
    private static String password = "root";

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 防止敏感信息泄露 -> 请手动调整配置
     *
     * @param args thank you, my friend !
     */
    public static void main(String[] args) {
        init(
                "", "tools-code", "",
                "cn.gmlee", "",
                "jdbc:oracle:thin:@scan-ip.exgrain.org:1521:ygwdb",
                "ldw_mall", "ldw_mall_"
        );
        execute();
    }


    /**
     * 参数初始化.
     *
     * @param system      系统名称
     * @param model       模块名称
     * @param project     项目名称
     * @param rootPackage 根路径
     * @param tablePrefix 前缀
     * @param url         数据库连接
     * @param username    用户名
     * @param password    密码
     */
    public final static void init(
            String system, String model, String project,
            String rootPackage, String tablePrefix,
            String url, String username, String password
    ) {
        CodeGenerator.system = system;
        CodeGenerator.model = model;
        CodeGenerator.project = project;
        CodeGenerator.rootPackage = rootPackage;
        CodeGenerator.parentPackage = getParentPackage();
        CodeGenerator.tablePrefix = tablePrefix;
        CodeGenerator.url = url;
        CodeGenerator.username = username;
        CodeGenerator.password = password;
    }

    public final static void execute() {
        String table = "file_business (不带前缀的表名)";
        while (!StringUtils.isBlank(table = scanner("表名"))) {
            String[] split = table.split(",");
            for (String tab : split) {
                // 代码生成器
                AutoGenerator mpg = mpg();
                mpg.getStrategy().setInclude(tab.trim());
                // 执行生成器
                mpg.execute();
            }
        }
    }


    private final static AutoGenerator mpg() {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 模板引擎
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());

        // 全局配置
        GlobalConfig gc = getGlobalConfig();
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = getDataSourceConfig();
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = getPackageConfig();
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = getInjectionConfig();
        mpg.setCfg(cfg);

        // 自定义模板
        TemplateConfig templateConfig = getTemplateConfig();
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = getStrategyConfig();
        mpg.setStrategy(strategy);

        return mpg;
    }

    private final static StrategyConfig getStrategyConfig() {
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(true);
        strategy.setControllerMappingHyphenStyle(true);
        strategy.setTablePrefix(tablePrefix);
        return strategy;
    }

    private final static TemplateConfig getTemplateConfig() {
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setService("templates/customService.java");
        templateConfig.setServiceImpl("templates/customServiceImpl.java");
        templateConfig.setController("templates/customController.java");
        return templateConfig;
    }

    private final static InjectionConfig getInjectionConfig() {
        String projectPath = System.getProperty("user.dir");
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // 在配置中使用 cfg.Vo 获取参数
                Map<String, Object> map = new HashMap(1);
                map.put("Vo", parentPackage + "." + "controller.vo");
                this.setMap(map);
            }
        };
        List<FileOutConfig> focList = new ArrayList<>();
        String voTemplatePath = "/templates/customVo.java.ftl";
        focList.add(new FileOutConfig(voTemplatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + getEmpty(system, "/") + getEmpty(model, "/") + getEmpty(project, "/") + "/src/test/java/" + parentPackage.replace(".", "/") + "/controller/vo"
                        + "/" + tableInfo.getEntityName() + "Vo" + StringPool.DOT_JAVA;
            }
        });
        cfg.setFileOutConfigList(focList);
        return cfg;
    }

    private final static PackageConfig getPackageConfig() {
        PackageConfig pc = new PackageConfig();
        pc.setParent(parentPackage);
        pc.setController("controller");
        pc.setService("service");
        pc.setServiceImpl("service.impl");
        pc.setMapper("dao.mapper");
        pc.setEntity("dao.entity");
        pc.setXml("dao.mapper");
        return pc;
    }

    private final static DataSourceConfig getDataSourceConfig() {
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(url);
        dsc.setDriverName(driverName);
        dsc.setUsername(username);
        dsc.setPassword(password);
        return dsc;
    }

    private final static GlobalConfig getGlobalConfig() {
        String projectPath = System.getProperty("user.dir");
        GlobalConfig gc = new GlobalConfig();
        gc.setOutputDir(projectPath + getEmpty(system, "/") + getEmpty(model, "/") + getEmpty(project, "/") + "/src/test/java");
        gc.setAuthor("Jas°");
        gc.setOpen(false);
        gc.setServiceName("%sService");
        gc.setMapperName("%sMapper");
        gc.setSwagger2(false);
        gc.setFileOverride(true);
        // 时间类型都用Date, 不用LocalDateTime等
        gc.setDateType(DateType.ONLY_DATE);
        gc.setBaseResultMap(true);
        gc.setBaseColumnList(true);
        return gc;
    }

    /**
     * <p>
     * 读取控制台内容
     * </p>
     *
     * @param tip the tip
     * @return the string
     */
    public final static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }
}
