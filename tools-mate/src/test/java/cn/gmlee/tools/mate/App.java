package cn.gmlee.tools.mate;

import cn.gmlee.tools.ds.config.druid.DruidMonitorAutoConfiguration;
import cn.gmlee.tools.mate.interceptor.DataAuthServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.List;

@ComponentScan(value = {"cn.gmlee.tools"})
@SpringBootApplication(exclude = {DruidMonitorAutoConfiguration.class, })
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public DataAuthServer dataAuthServer(){
        return new DataAuthServer() {
            @Override
            public boolean printSql() {
                return true;
            }

            @Override
            public String rowIn(String flag) {
                return "1";
            }

            @Override
            public List<String> rowFields(String flag) {
                return Arrays.asList("sys_id");
            }

            @Override
            public boolean colFilter(String flag, String column) {
                return true;
            }

            @Override
            public List<String> colFields(String flag) {
                return null;
            }
        };
    }
}
