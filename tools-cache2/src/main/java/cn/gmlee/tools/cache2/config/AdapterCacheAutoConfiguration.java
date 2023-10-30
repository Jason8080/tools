package cn.gmlee.tools.cache2.config;


import cn.gmlee.tools.cache2.adapter.BaseFieldAdapter;
import cn.gmlee.tools.cache2.adapter.CollectionFieldAdapter;
import cn.gmlee.tools.cache2.adapter.FieldAdapter;
import cn.gmlee.tools.cache2.adapter.ObjectFieldAdapter;
import org.springframework.context.annotation.Bean;

public class AdapterCacheAutoConfiguration {

    @Bean
    public FieldAdapter baseFieldAdapter() {
        return new BaseFieldAdapter();
    }

    @Bean
    public FieldAdapter collectionFieldAdapter() {
        return new CollectionFieldAdapter();
    }

    @Bean
    public FieldAdapter objectFieldAdapter() {
        return new ObjectFieldAdapter();
    }

}
