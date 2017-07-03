package org.molgenis.security.core.runas;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.Advised;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;

/**
 * Proxy that set a SystemSecurityToken in the security context for the duration of a method
 */
public class RunAsSystemProxy implements Advice, MethodInterceptor
{
	private final Object targetObject;

	public RunAsSystemProxy(Object targetObject)
	{
		this.targetObject = targetObject;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable
	{
		Method interfaceMethod = invocation.getMethod();

		Class<?> clazz = targetObject instanceof Advised ? ((Advised) targetObject).getTargetClass() : targetObject.getClass();

		Method targetMethod = clazz.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());

		if (!targetMethod.isAnnotationPresent(RunAsSystem.class))
		{
			return invocation.proceed();
		}

		return runAsSystem(invocation::proceed);

	}

	public static void runAsSystem(Runnable runnable)
	{
		runAsSystem(() ->
		{
			runnable.run();
			return null;
		});
	}

	public static <T, X extends Throwable> T runAsSystem(RunnableAsSystem<T, X> runnable) throws X
	{
		// Remember the original context
		SecurityContext origCtx = SecurityContextHolder.getContext();
		try
		{
			// Set a SystemSecurityToken
			SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
			SecurityContextHolder.getContext().setAuthentication(new SystemSecurityToken());
			return runnable.run();
		}
		finally
		{
			// Set the original context back when method is finished
			SecurityContextHolder.setContext(origCtx);
		}
	}
}
