package org.molgenis.security.core.runas;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Proxy that set a SystemSecurityToken in the security context for the duration of a method
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Aspect
@Component
public class RunAsSystemAspect
{
	@Around("@annotation(RunAsSystem)")
	public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable
	{
		return runAsSystem((RunnableAsSystem<Object, Throwable>) joinPoint::proceed);
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
