package org.molgenis.ui;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class MolgenisWebAppInitializer
{
	private static final Logger logger = Logger.getLogger(MolgenisWebAppInitializer.class);

	/**
	 * A Molgenis common web application initializer
	 * 
	 * @param servletContext
	 * @param appConfig
	 * @param isDasUsed
	 *            is the molgenis-omx-das module used?
	 * @throws ServletException
	 */
	protected void onStartup(ServletContext servletContext, Class<?> appConfig, boolean isDasUsed)
			throws ServletException
	{
		// Create the 'root' Spring application context
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
		rootContext.register(appConfig);
		
		// Manage the lifecycle of the root application context
		servletContext.addListener(new ContextLoaderListener(rootContext));

		// Register and map the dispatcher servlet
		ServletRegistration.Dynamic dispatcherServlet = servletContext.addServlet("dispatcher", new DispatcherServlet(
				rootContext));
		if (dispatcherServlet == null)
		{
			logger.warn("ServletContext already contains a complete ServletRegistration for servlet 'dispatcher'");
		}
		else
		{
			final int maxSize = 32 * 1024 * 1024;
			int loadOnStartup = (isDasUsed ? 2 : 1);
			dispatcherServlet.setLoadOnStartup(loadOnStartup);
			dispatcherServlet.addMapping("/");
			dispatcherServlet.setMultipartConfig(new MultipartConfigElement(null, maxSize, maxSize, maxSize));
			dispatcherServlet.setInitParameter("dispatchOptionsRequest", "true");
		}

		// add filters
		javax.servlet.FilterRegistration.Dynamic etagFilter = servletContext.addFilter("etagFilter",
				new ShallowEtagHeaderFilter());
		etagFilter.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST), true, "dispatcher");

		// enable use of request scoped beans in FrontController
		servletContext.addListener(new RequestContextListener());
	}
}