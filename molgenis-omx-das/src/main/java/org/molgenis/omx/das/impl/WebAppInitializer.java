package org.molgenis.omx.das.impl;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.log4j.Logger;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import uk.ac.ebi.mydas.controller.MydasServlet;

public class WebAppInitializer implements WebApplicationInitializer
{
	private static final Logger logger = Logger.getLogger(WebAppInitializer.class);

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException
	{
		AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
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
	}
}