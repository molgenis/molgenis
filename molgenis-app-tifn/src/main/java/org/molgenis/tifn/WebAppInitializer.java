package org.molgenis.tifn;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import app.servlet.FrontController;

public class WebAppInitializer implements WebApplicationInitializer
{
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException
	{
		AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
		ctx.register(WebAppConfig.class);

		// spring
		Dynamic dispatcherServlet = servletContext.addServlet("dispatcher", new DispatcherServlet(ctx));
		dispatcherServlet.setLoadOnStartup(1);
		dispatcherServlet.addMapping("/");

		// molgenis
		Dynamic frontControllerServlet = servletContext.addServlet("front-controller", new FrontController());
		frontControllerServlet.setLoadOnStartup(2);
	}
}