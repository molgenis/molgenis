package org.molgenis.omx;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.log4j.Logger;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import app.servlet.FrontController;

public class WebAppInitializer implements WebApplicationInitializer
{
	private static Logger logger = Logger.getLogger(WebAppInitializer.class);

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException
	{
		AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
		ctx.register(WebAppConfig.class);

		// spring
		Dynamic dispatcherServlet = servletContext.addServlet("dispatcher", new DispatcherServlet(ctx));
		if (dispatcherServlet == null)
		{
			logger.warn("ServletContext already contains a complete ServletRegistration for servlet 'dispatcher'");
		}
		else
		{
			dispatcherServlet.setLoadOnStartup(1);
			dispatcherServlet.addMapping("/");
		}

		// molgenis
		Dynamic frontControllerServlet = servletContext.addServlet("front-controller", new FrontController());
		if (frontControllerServlet == null)
		{
			logger.warn("ServletContext already contains a complete ServletRegistration for servlet 'front-controller'");
		}
		else
		{
			frontControllerServlet.setLoadOnStartup(2);
		}
	}
}