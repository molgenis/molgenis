package org.molgenis.security.core.runas;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Proxies class that have the RunAsSystem annotation on one of its methods
 */
@Component
public class RunAsSystemBeanPostProcessor implements BeanPostProcessor
{

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
	{
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
	{
		Class<?> clazz = bean.getClass();

		if (bean instanceof Advised && ((Advised) bean).getTargetClass() != null)
		{
			Advised advised = (Advised) bean;
			for (Method method : advised.getTargetClass().getMethods())
			{
				if (method.isAnnotationPresent(RunAsSystem.class))
				{
					advised.addAdvice(new RunAsSystemProxy(bean));
					return bean;
				}
			}
		}
		else
		{
			for (Method method : clazz.getMethods())
			{
				if (method.isAnnotationPresent(RunAsSystem.class))
				{
					ProxyFactory pf = new ProxyFactory();
					pf.setTarget(bean);
					pf.setInterfaces(clazz.getInterfaces());
					pf.addAdvice(new RunAsSystemProxy(bean));

					return pf.getProxy();
				}
			}
		}

		return bean;
	}

}
