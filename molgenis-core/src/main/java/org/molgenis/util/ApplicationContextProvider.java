package org.molgenis.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Can be used by legacy classes to get a reference to the application context
 * 
 * @author erwin
 * 
 */
public class ApplicationContextProvider implements ApplicationContextAware
{
	private static ApplicationContext ctx = null;

	public static ApplicationContext getApplicationContext()
	{
		return ctx;
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException
	{
		ApplicationContextProvider.ctx = ctx;
	}
}
