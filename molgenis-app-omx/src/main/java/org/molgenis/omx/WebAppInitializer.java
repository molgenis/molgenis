package org.molgenis.omx;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.log4j.Logger;
import org.molgenis.omx.das.DasPatientFilter;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import uk.ac.ebi.mydas.controller.MydasServlet;

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
			dispatcherServlet.setLoadOnStartup(1);
			dispatcherServlet.addMapping("/");
			dispatcherServlet.setMultipartConfig(new MultipartConfigElement(null, maxSize, maxSize, maxSize));
			dispatcherServlet.setInitParameter("dispatchOptionsRequest", "true");
		}
		
		//Filter is needed to alter the urls used to serve patient specific URLs
		javax.servlet.FilterRegistration.Dynamic filter = servletContext.addFilter("dasFilter", new DasPatientFilter());
		if (filter == null)
		{
			logger.warn("ServletContext already contains a complete FilterRegistration for servlet 'dasFilter'");
		}
		else
		{
			filter.addMappingForUrlPatterns(EnumSet.of (DispatcherType.REQUEST), true, "/das/*");
		}
		
		Dynamic dasServlet = servletContext.addServlet("dasServlet", new MydasServlet());
		if (dasServlet == null)
		{
			logger.warn("ServletContext already contains a complete ServletRegistration for servlet 'dasServlet'");
		}
		else
		{
			dasServlet.setLoadOnStartup(2);
			dasServlet.addMapping("/das/*");
		}

		// enable use of request scoped beans in FrontController
		servletContext.addListener(new RequestContextListener());
	}
}