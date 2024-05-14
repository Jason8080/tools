package cn.gmlee.tools.mate;

import cn.gmlee.tools.ds.config.druid.DruidMonitorAutoConfiguration;
import cn.gmlee.tools.mate.interceptor.DataAuthServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ComponentScan(value = {"cn.gmlee.tools"})
@SpringBootApplication(exclude = {DruidMonitorAutoConfiguration.class, })
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    /**
     * 实现数据鉴权只需两步
     * ① 在需要数据鉴权的方法上添加@DataScope("flag")注解
     * ② 实现DataAuthServer服务接口 (核心在于动态获取自身企业的权限配置列表)
     * @return.
     */
    @Bean
    public DataAuthServer dataAuthServer(){
        return new DataAuthServer() {
            @Override
            public boolean printSql() {
                return true;
            }

            @Override
            public boolean allowEx() {
                return true;
            }

            @Override
            public Map<String, List> rowWheres(String flag) {
                // 构建过滤条件:
                Map<String, List> wheres = new HashMap<>();
                // # 仅查看自己的数据 (假如当前登录用户ID = 1)
                wheres.put("created_by", Arrays.asList(1));
                // # 仅查看能看的数据 (假如当前用户能看auth_type = 2的数据)
                wheres.put("auth_type", Arrays.asList("1"));
                // 以上只是举例, 实际上可以是: 行政区域、组织架构、商家编号、大区编号...且可以同时使用
                return wheres;
            }

            @Override
            public boolean colFilter(String flag, String column) {
                // 需要鉴权的都不通过
                return false;
            }

            @Override
            public List<String> colFields(String flag) {
                // 仅对敏感数据鉴权
                return Arrays.asList("request_ip");
            }
        };
    }
}
