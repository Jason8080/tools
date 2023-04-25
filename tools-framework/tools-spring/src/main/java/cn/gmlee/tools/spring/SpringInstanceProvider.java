package cn.gmlee.tools.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;


/**
 * Spring 上下文工具.
 *
 * @author Jas °
 */
public class SpringInstanceProvider {

	private ApplicationContext applicationContext;

	/**
	 * 以一批spring配置文件的路径初始化spring实例提供者。
	 *
	 * @param locations spring配置文件的路径的集合。spring将从类路径开始获取这批资源文件。
	 */
	public SpringInstanceProvider(String... locations) {
		applicationContext = new ClassPathXmlApplicationContext(locations);
	}

	/**
	 * 从ApplicationContext生成SpringProvider
	 *
	 * @param applicationContext the application context
	 */
	public SpringInstanceProvider(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * 根据一批Spring配置文件初始化spring实例提供者。
	 *
	 * @param annotatedClasses the annotated classes
	 */
	public SpringInstanceProvider(Class<?>... annotatedClasses) {
		applicationContext = new AnnotationConfigApplicationContext(
				annotatedClasses);
	}

	/**
	 * 返回指定类型的实例。
	 *
	 * @param <T>       the type parameter
	 * @param beanClass 实例的类型
	 * @return 指定类型的实例 。
	 */
	public <T> T getBean(Class<T> beanClass) {
		String[] beanNames = applicationContext.getBeanNamesForType(beanClass);
		if (beanNames.length == 0) {
			return null;
		}
		return (T) applicationContext.getBean(beanNames[0]);
	}

	/**
	 * 根据类型 and 名称 获取对象.
	 *
	 * @param <T>       the type parameter
	 * @param beanClass the bean class
	 * @param beanName  the bean name
	 * @return the instance
	 */
	public <T> T getBean(Class<T> beanClass, String beanName) {
		return (T) applicationContext.getBean(beanName, beanClass);
	}

	/**
	 * 根据 名称 获取对象.
	 *
	 * @param <T>      the type parameter
	 * @param beanName the bean name
	 * @return the instance
	 */
	public <T> T getBean(String beanName) {
		return (T) applicationContext.getBean(beanName);
	}

	/**
	 * 获取 对象 的数量.
	 *
	 * @param <T>       the type parameter
	 * @param beanClass the bean class
	 * @return the interface count
	 */
	public <T> int getCount(Class<T> beanClass) {
		return applicationContext.getBeanNamesForType(beanClass).length;
	}

	/**
	 * 获取 接口 下的对象集合.
	 *
	 * @param <T>       the type parameter
	 * @param beanClass the bean class
	 * @return the interfaces
	 */
	public <T> Map<String, T> getInterfaces(Class<T> beanClass) {
		return applicationContext.getBeansOfType(beanClass);
	}

	/**
	 * 获取上下文对象.
	 *
	 * @return the application context
	 */
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
}
