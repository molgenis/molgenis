package org.molgenis.omx;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.log4j.Logger;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class WebAppInitializer implements WebApplicationInitializer
{
	private static final Logger logger = Logger.getLogger(WebAppInitializer.class);

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException
	{
		AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
		ctx.register(WebAppConfig.class);

		// manage the lifecycle of the root application context
		servletContext.addListener(new ContextLoaderListener(ctx));

		// spring
		Dynamic dispatcherServlet = servletContext.addServlet("dispatcher", new DispatcherServlet(ctx));
		if (dispatcherServlet == null)
		{
			logger.warn("ServletContext already contains a complete ServletRegistration for servlet 'dispatcher'");
		}
		else
		{
			final int maxSize = 32 * 1024 * 1024;
			dispatcherServlet.setLoadOnStartup(2);
			dispatcherServlet.addMapping("/");
			dispatcherServlet.setMultipartConfig(new MultipartConfigElement(null, maxSize, maxSize, maxSize));
			dispatcherServlet.setInitParameter("dispatchOptionsRequest", "true");
		}

		// add filters
		javax.servlet.FilterRegistration.Dynamic etagFilter = servletContext.addFilter("etagFilter",
				new ShallowEtagHeaderFilter());
		etagFilter.addMappingForServletNames(null, true, "dispatcher");

		// enable use of request scoped beans in FrontController
		servletContext.addListener(new RequestContextListener());
	}
}