package cn.gmlee.tools.mybatis.assist;

import cn.gmlee.tools.base.util.BoolUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 通用mybatis配置
 *
 * @author Jas °
 * @date 2021 /2/2 (周二)
 */
public class ConfigurationAssist {
    /**
     * 打印日志.
     *
     * @param clazz the clazz
     */
    public static void log(Class clazz) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.debug("启用{}配置", clazz.getName());
    }

    /**
     * 覆盖Mybatis配置.
     *
     * @param <C> the type parameter
     * @param c   the c
     * @return the c
     */
    public static <C extends Configuration> C addSetting(C c) {
        if (c != null) {
            // 返回主键
            c.setUseGeneratedKeys(true);
            // 空值转换
            c.setJdbcTypeForNull(JdbcType.NULL);
            // 驼峰命名
            c.setMapUnderscoreToCamelCase(true);
        }
        return c;
    }

    /**
     * Add interceptor.
     *
     * @param configuration the configuration
     * @param interceptors  the interceptors
     */
    public static void addInterceptor(Configuration configuration, Interceptor... interceptors) {
        if (BoolUtil.notEmpty(interceptors)) {
            for (Interceptor interceptor : interceptors) {
                configuration.addInterceptor(interceptor);
            }
        }
    }

    public static void addInterceptor(Configuration configuration, List<Interceptor> interceptors) {
        if (BoolUtil.notEmpty(interceptors)) {
            for (Interceptor interceptor : interceptors) {
                configuration.addInterceptor(interceptor);
            }
        }
    }

    public static void addPagination(MybatisConfiguration configuration, List<Interceptor> interceptors) {
        boolean exist = false;
        List<Interceptor> all = new ArrayList();
        if (BoolUtil.notEmpty(interceptors)) {
            all.addAll(interceptors);
        }
        all.addAll(configuration.getInterceptors());
        for (Object interceptor : all) {
            if (interceptor instanceof MybatisPlusInterceptor) {
                List<InnerInterceptor> is = ((MybatisPlusInterceptor) interceptor).getInterceptors();
                Optional<InnerInterceptor> any = is.stream().filter(x -> x instanceof PaginationInnerInterceptor).findAny();
                exist = any.isPresent();
            }

        }
        if (!exist) {
            MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
            interceptor.addInnerInterceptor(createPaginationInnerInterceptor());
            configuration.addInterceptor(interceptor);
        }
    }

    private static PaginationInnerInterceptor createPaginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setOptimizeJoin(false);
        return paginationInnerInterceptor;
    }
}
