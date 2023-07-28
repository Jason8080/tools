package cn.gmlee.tools.prevent.aspect;


import cn.gmlee.tools.prevent.annotation.Limit;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.lang.NonNull;

/**
 * 防刷AOP通知
 *
 * @author James
 * @since 2023-07-26
 */
public class LimitAnnotationAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {
    private static final long serialVersionUID = -412870624425696792L;

    private final Advice advice;

    private final Pointcut pointcut = AnnotationMatchingPointcut.forMethodAnnotation(Limit.class);

    public LimitAnnotationAdvisor(@NonNull LimitInterceptor limitInterceptor, int order) {
        this.advice = limitInterceptor;
        setOrder(order);
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }
}
