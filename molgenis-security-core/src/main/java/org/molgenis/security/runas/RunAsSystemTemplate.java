package org.molgenis.security.runas;

import java.util.concurrent.Callable;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Run a lambda code block as system.
 * 
 * Example:
 * 
 * Entity entity = RunAsSystemTemplate.runAsSystem(() -> { return molgenisUserService.getUser(username); });
 */
public class RunAsSystemTemplate
{
	public static <T> T runAsSystem(Callable<T> callback)
	{
		// Remember the original context
		SecurityContext origCtx = SecurityContextHolder.getContext();
		try
		{
			// Set a SystemSecurityToken
			SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
			SecurityContextHolder.getContext().setAuthentication(new SystemSecurityToken());

			return callback.call();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			// Set the original context back when method is finished
			SecurityContextHolder.setContext(origCtx);
		}
	}
}
