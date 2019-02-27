package org.molgenis.core.ui;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import org.molgenis.core.ui.browserdetection.BrowserDetectionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class MolgenisWebAppInitializer {
  private static final Logger LOG = LoggerFactory.getLogger(MolgenisWebAppInitializer.class);

  /** A Molgenis common web application initializer */
  protected void onStartup(ServletContext servletContext, Class<?> appConfig) {
    // Create the 'root' Spring application context
    AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
    rootContext.setAllowBeanDefinitionOverriding(false);
    rootContext.register(appConfig);

    // Manage the lifecycle of the root application context
    servletContext.addListener(new ContextLoaderListener(rootContext));

    // Register and map the dispatcher servlet
    DispatcherServlet dispatcherServlet = new DispatcherServlet(rootContext);
    dispatcherServlet.setDispatchOptionsRequest(true);
    // instead of throwing a 404 when a handler is not found allow for custom handling
    dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);

    ServletRegistration.Dynamic dispatcherServletRegistration =
        servletContext.addServlet("dispatcher", dispatcherServlet);
    if (dispatcherServletRegistration == null) {
      LOG.warn(
          "ServletContext already contains a complete ServletRegistration for servlet 'dispatcher'");
    } else {
      dispatcherServletRegistration.setAsyncSupported(true);
      dispatcherServletRegistration.addMapping("/");
    }

    // add filters
    Dynamic browserDetectionFiler =
        servletContext.addFilter("browserDetectionFilter", BrowserDetectionFilter.class);
    browserDetectionFiler.setAsyncSupported(true);
    browserDetectionFiler.addMappingForUrlPatterns(
        EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), false, "*");

    Dynamic etagFilter = servletContext.addFilter("etagFilter", ShallowEtagHeaderFilter.class);
    etagFilter.setAsyncSupported(true);
    etagFilter.addMappingForServletNames(
        EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), true, "dispatcher");

    // enable use of request scoped beans in FrontController
    servletContext.addListener(new RequestContextListener());

    servletContext.addListener(HttpSessionEventPublisher.class);
  }
}
