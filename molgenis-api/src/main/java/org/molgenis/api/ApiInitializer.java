package org.molgenis.api;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class ApiInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
  private static final int MB = 1024 * 1024;

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    super.onStartup(servletContext);
    servletContext.addListener(new HttpSessionEventPublisher());
  }

  @Override
  protected FrameworkServlet createDispatcherServlet(WebApplicationContext servletAppContext) {
    FrameworkServlet frameworkServlet = super.createDispatcherServlet(servletAppContext);
    if (frameworkServlet instanceof DispatcherServlet) {
      ((DispatcherServlet) frameworkServlet).setThrowExceptionIfNoHandlerFound(true);
    }
    return frameworkServlet;
  }

  @Override
  protected void customizeRegistration(Dynamic registration) {
    registration.setMultipartConfig(
        new MultipartConfigElement(null, 128L * MB, 128L * MB, 16 * MB));
    registration.setAsyncSupported(true);
  }

  @Override
  protected Class<?>[] getRootConfigClasses() {
    return null;
  }

  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class[] {ApiConfig.class};
  }

  @Override
  protected String[] getServletMappings() {
    return new String[] {"/api/*"};
  }

  @Override
  protected String getServletName() {
    return "api-" + super.getServletName();
  }
}
