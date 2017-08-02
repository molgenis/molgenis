package org.molgenis.security.core.runas;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionInterceptor;

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
					addAdvice(advised, new RunAsSystemProxy(bean));
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

	/**
	 * Add RunAsSystemProxy advice to the list of advisors of the given Advised. The list location is before
	 * the TransactionInterceptor, see https://github.com/molgenis/molgenis/issues/6421.
	 */
	private void addAdvice(Advised advised, RunAsSystemProxy runAsSystemProxy)
	{
		Advisor[] advisors = advised.getAdvisors();
		int i;
		for (i = 0; i < advisors.length; ++i)
		{
			Advice advice = advisors[i].getAdvice();
			if (advice instanceof TransactionInterceptor)
			{
				break;
			}
		}
		advised.addAdvice(i, runAsSystemProxy);
	}
}
