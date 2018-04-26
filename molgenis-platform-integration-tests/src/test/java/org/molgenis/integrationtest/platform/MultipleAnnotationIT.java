package org.molgenis.integrationtest.platform;

import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { MultipleAnnotationIT.Config.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
public class MultipleAnnotationIT extends AbstractTestNGSpringContextTests
{
	@Autowired
	private BeanClass bean;

	@Test
	@WithMockUser
	public void testMethodAnnotationOrder()
	{
		// Tests that RunAsSystem aspect takes place AFTER spring auth checks and BEFORE spring transaction management
		// and that its order is independent of the annotation order.
		assertEquals(getCurrentUsername(), "user");
		bean.runAsSystemMethod();
		assertEquals(getCurrentUsername(), "user");
		bean.runAsSystemMethod2();
		assertEquals(getCurrentUsername(), "user");
	}

	public static class BeanClass
	{
		@RunAsSystem
		@Transactional
		@PreAuthorize("ROLE_USER")
		public void runAsSystemMethod()
		{
			assertEquals(getCurrentUsername(), "SYSTEM");
		}

		@PreAuthorize("ROLE_USER")
		@RunAsSystem
		@Transactional
		public void runAsSystemMethod2()
		{
			assertEquals(getCurrentUsername(), "SYSTEM");
		}
	}

	private static String getCurrentUsername()
	{
		return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
	}

	@EnableTransactionManagement
	@EnableAspectJAutoProxy
	@Import(RunAsSystemAspect.class)
	@Configuration
	public static class Config
	{
		@Bean
		public BeanClass bean()
		{
			return new BeanClass();
		}

		@Bean
		public PlatformTransactionManager transactionManager()
		{
			return new AbstractPlatformTransactionManager()
			{
				@Override
				protected Object doGetTransaction() throws TransactionException
				{
					assertEquals(getCurrentUsername(), "SYSTEM");
					return new Object();
				}

				@Override
				protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException
				{
					assertEquals(getCurrentUsername(), "SYSTEM");
				}

				@Override
				protected void doCommit(DefaultTransactionStatus status) throws TransactionException
				{
					assertEquals(getCurrentUsername(), "SYSTEM");
				}

				@Override
				protected void doRollback(DefaultTransactionStatus status) throws TransactionException
				{

				}
			};
		}

	}
}
