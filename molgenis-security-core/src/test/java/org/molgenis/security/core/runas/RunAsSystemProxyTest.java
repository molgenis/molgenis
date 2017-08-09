package org.molgenis.security.core.runas;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import static org.testng.Assert.*;

public class RunAsSystemProxyTest
{
	private static Authentication AUTHENTICATION_PREVIOUS;

	@BeforeClass
	public void setUpBeforeClass()
	{
		AUTHENTICATION_PREVIOUS = SecurityContextHolder.getContext().getAuthentication();
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_PREVIOUS);
	}

	@Test
	public void invoke() throws NoSuchMethodException, SecurityException, Throwable
	{
		assertNull(SecurityContextHolder.getContext().getAuthentication());

		final TestProxy t = new TestProxy();
		RunAsSystemProxy proxy = new RunAsSystemProxy(t);
		proxy.invoke(new MethodInvocation()
		{
			@Override
			public Object[] getArguments()
			{
				return new Object[] {};
			}

			@Override
			public Object proceed() throws Throwable
			{
				t.run();
				return null;
			}

			@Override
			public Object getThis()
			{
				return t;
			}

			@Override
			public AccessibleObject getStaticPart()
			{
				return null;
			}

			@Override
			public Method getMethod()
			{
				try
				{
					return Runnable.class.getMethod("run");
				}
				catch (NoSuchMethodException e)
				{
					throw new RuntimeException(e);
				}
			}

		});

		// Check if run method of the TestProxy object has been called
		assertTrue(t.runCalled);

		// The SystemSecurityToken should have been removed after invocation of the run method and replaced with the
		// original ctx wich did not contain any athentication objects
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	private static class TestProxy implements Runnable
	{
		private boolean runCalled = false;

		@Override
		@RunAsSystem
		public void run()
		{
			runCalled = true;
			SecurityContext ctx = SecurityContextHolder.getContext();

			// Here we should have the SystemSecurityToken
			assertEquals(ctx.getAuthentication(), new SystemSecurityToken());
		}
	}
}
