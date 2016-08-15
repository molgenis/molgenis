package org.molgenis.das.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import uk.ac.ebi.mydas.controller.MydasServlet;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;
import java.util.EnumSet;

public class WebAppInitializer implements WebApplicationInitializer
{
	private static final Logger LOG = LoggerFactory.getLogger(WebAppInitializer.class);

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException
	{
		//Filter is needed to alter the urls used to serve patient specific URLs
		javax.servlet.FilterRegistration.Dynamic filter = servletContext.addFilter("dasFilter", new DasURLFilter());
		if (filter == null)
		{
			LOG.warn("ServletContext already contains a complete FilterRegistration for servlet 'dasFilter'");
		}
		else
		{
			filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/das/*");
		}

		Dynamic dasServlet = servletContext.addServlet("dasServlet", new MydasServlet());
		if (dasServlet == null)
		{
			LOG.warn("ServletContext already contains a complete ServletRegistration for servlet 'dasServlet'");
		}
		else
		{
			dasServlet.setLoadOnStartup(1);
			dasServlet.addMapping("/das/*");
		}
	}
}