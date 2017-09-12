package org.molgenis.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Can be used by legacy classes to get a reference to the application context
 *
 * @author erwin
 */
@SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Intented static write from instance")
public class ApplicationContextProvider implements ApplicationContextAware
{
	private static ApplicationContext ctx = null;

	public static ApplicationContext getApplicationContext()
	{
		return ctx;
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx)
	{
		ApplicationContextProvider.ctx = ctx;
	}
}
