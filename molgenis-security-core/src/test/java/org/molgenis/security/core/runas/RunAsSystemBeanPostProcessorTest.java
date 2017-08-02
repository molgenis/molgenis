package org.molgenis.security.core.runas;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class RunAsSystemBeanPostProcessorTest
{

	@Test
	public void postProcessAfterInitialization()
	{
		// Should return a proxy so should not be equal to this
		assertNotEquals(new RunAsSystemBeanPostProcessor().postProcessAfterInitialization(this, "Test"), this);

		Object o = new Object();
		// Object does not have a method with the RunAsSystem annotation so should not return a proxy
		assertEquals(new RunAsSystemBeanPostProcessor().postProcessAfterInitialization(o, "Test"), o);
	}

	@DataProvider(name = "testPostProcessAfterInitializationAdviceLocationProvider")
	public static Iterator<Object[]> testPostProcessAfterInitializationAdviceLocationProvider()
	{
		Advisor methodSecurityInterceptorAdvisor = mock(Advisor.class);
		when(methodSecurityInterceptorAdvisor.toString()).thenReturn("methodSecurityInterceptorAdvisor");
		when(methodSecurityInterceptorAdvisor.getAdvice()).thenReturn(mock(MethodSecurityInterceptor.class));

		Advisor transactionInterceptorAdvisor = mock(Advisor.class);
		when(transactionInterceptorAdvisor.toString()).thenReturn("transactionInterceptorAdvisor");
		when(transactionInterceptorAdvisor.getAdvice()).thenReturn(mock(TransactionInterceptor.class));

		List<Object[]> dataList = new ArrayList<>();
		dataList.add(new Object[] { new Advisor[] { methodSecurityInterceptorAdvisor }, 1 });
		dataList.add(new Object[] { new Advisor[] { transactionInterceptorAdvisor }, 0 });
		dataList.add(
				new Object[] { new Advisor[] { methodSecurityInterceptorAdvisor, transactionInterceptorAdvisor }, 1 });
		return dataList.iterator();
	}

	@SuppressWarnings("unchecked")
	@Test(dataProvider = "testPostProcessAfterInitializationAdviceLocationProvider")
	public void testPostProcessAfterInitializationAdviceLocation(Advisor[] advisors, int expectedPos)
	{
		Advised advised = mock(Advised.class);
		@SuppressWarnings("RawTypeCanBeGeneric")
		Class targetClazz = this.getClass();
		when(advised.getTargetClass()).thenReturn(targetClazz);
		when(advised.getAdvisors()).thenReturn(advisors);
		new RunAsSystemBeanPostProcessor().postProcessAfterInitialization(advised, null);
		verify(advised).addAdvice(eq(expectedPos), any(RunAsSystemProxy.class));
	}

	@Test
	public void postProcessBeforeInitialization()
	{
		assertEquals(new RunAsSystemBeanPostProcessor().postProcessBeforeInitialization(this, "Test"), this);
	}

	@RunAsSystem
	public void test()
	{
	}
}
