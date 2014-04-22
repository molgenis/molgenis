package org.molgenis.omx;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.molgenis.omx.biobankconnect.applicationevent.SessionExpireListener;
import org.molgenis.ui.MolgenisWebAppInitializer;
import org.springframework.web.WebApplicationInitializer;

public class WebAppInitializer extends MolgenisWebAppInitializer implements WebApplicationInitializer
{
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException
	{
		super.onStartup(servletContext, WebAppConfig.class, true);

		// Add a session expire lister for omx biobankconnect
		servletContext.addListener(new SessionExpireListener());
	}
}