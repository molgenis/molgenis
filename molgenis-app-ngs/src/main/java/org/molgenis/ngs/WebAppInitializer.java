package org.molgenis.ngs;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.log4j.Logger;
import org.molgenis.servlet.FrontController;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class WebAppInitializer implements WebApplicationInitializer
{
	private static final Logger logger = Logger.getLogger(WebAppInitializer.class);

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
			final int maxSize = 32 * 1024 * 1024;
			dispatcherServlet.setLoadOnStartup(1);
			dispatcherServlet.addMapping("/");
			dispatcherServlet.setMultipartConfig(new MultipartConfigElement(null, maxSize, maxSize, maxSize));
			dispatcherServlet.setInitParameter("dispatchOptionsRequest", "true");
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
			frontControllerServlet.addMapping("/molgenis.do");
			frontControllerServlet.addMapping("/xref/find"); // org.molgenis.framework.server.services.MolgenisXrefService
			frontControllerServlet.addMapping("/tmpfile"); // org.molgenis.framework.server.services.MolgenisTmpFileService
		}

		// enable use of request scoped beans in FrontController
		servletContext.addListener(new RequestContextListener());
	}
}