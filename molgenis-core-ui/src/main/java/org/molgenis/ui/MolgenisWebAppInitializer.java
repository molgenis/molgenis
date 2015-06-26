package org.molgenis.ui;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.molgenis.security.CorsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class MolgenisWebAppInitializer
{
	private static final int MB = 1024 * 1024;
	// the size threshold after which multi-part files will be written to disk.
	private static final int FILE_SIZE_THRESHOLD = 10 * MB;
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisWebAppInitializer.class);

	protected void onStartup(ServletContext servletContext, Class<?> appConfig, boolean isDasUsed)
			throws ServletException
	{
		// no maximum field size provided? default to 32 Mb
		onStartup(servletContext, appConfig, isDasUsed, 32);
	}

	/**
	 * A Molgenis common web application initializer
	 * 
	 * @param servletContext
	 * @param appConfig
	 * @param isDasUsed
	 *            is the molgenis-das module used?
	 * @throws ServletException
	 */
	protected void onStartup(ServletContext servletContext, Class<?> appConfig, boolean isDasUsed, int maxFileSize)
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
			LOG.warn("ServletContext already contains a complete ServletRegistration for servlet 'dispatcher'");
		}
		else
		{
			final long maxSize = (long) maxFileSize * MB;
			int loadOnStartup = (isDasUsed ? 2 : 1);
			dispatcherServlet.setLoadOnStartup(loadOnStartup);
			dispatcherServlet.addMapping("/");
			dispatcherServlet
					.setMultipartConfig(new MultipartConfigElement(null, maxSize, maxSize, FILE_SIZE_THRESHOLD));
			dispatcherServlet.setInitParameter("dispatchOptionsRequest", "true");

		}

		// add filters
		Dynamic etagFilter = servletContext.addFilter("etagFilter", ShallowEtagHeaderFilter.class);
		etagFilter.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST), true, "dispatcher");
		Dynamic corsFilter = servletContext.addFilter("corsFilter", CorsFilter.class);
		corsFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/api/*");

		// enable use of request scoped beans in FrontController
		servletContext.addListener(new RequestContextListener());
	}
}