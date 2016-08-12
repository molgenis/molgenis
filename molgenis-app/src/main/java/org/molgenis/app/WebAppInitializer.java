package org.molgenis.app;

import org.molgenis.ui.MolgenisWebAppInitializer;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class WebAppInitializer extends MolgenisWebAppInitializer implements WebApplicationInitializer
{
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException
	{
		super.onStartup(servletContext, WebAppConfig.class, true, 150);
	}
}