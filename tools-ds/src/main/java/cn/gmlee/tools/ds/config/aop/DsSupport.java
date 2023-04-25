package cn.gmlee.tools.ds.config.aop;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.ds.anno.Ds;
import cn.gmlee.tools.ds.dynamic.DynamicDataSourceHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 只读数据源切换
 * <p>
 *
 * @author Jas °
 * @Order(-2) 数值越小越靠前, 靠前被靠后覆盖 不能加: @ConditionalOnBean(DynamicDataSource.class), 因为先注册切面再注册DynamicDataSource对象 </p>
 * @date 2020 /8/20 12:13
 */
@Aspect
@Component
@Order(-1)
public class DsSupport {

    private static final Logger log = LoggerFactory.getLogger(DsSupport.class);


    /**
     * 谨记: 使注解生效必须通过代理对象调用
     *
     * @param point the point
     * @param ds    the ds
     * @return the object
     * @throws Throwable the throwable
     */
    @Around("@annotation(ds)")
    public Object ds(ProceedingJoinPoint point, Ds ds) throws Throwable {
        DynamicDataSourceHolder.count(1);
        String old = DynamicDataSourceHolder.get();
        DynamicDataSourceHolder.set(random(ds.value()));
        try {
            return point.proceed();
        } finally {
            DynamicDataSourceHolder.count(-1);
            DynamicDataSourceHolder.set(old);
            if(DynamicDataSourceHolder.zero()) {
                //清楚DbType一方面为了避免内存泄漏，更重要的是避免对后续在本线程上执行的操作产生影响
                DynamicDataSourceHolder.clear();
                log.info("清除threadLocal");
            }
        }
    }

    private String random(String... values) {
        if (BoolUtil.isEmpty(values)) {
            return null;
        }
        int i = new Random().nextInt(values.length);
        return values[i];
    }
}
