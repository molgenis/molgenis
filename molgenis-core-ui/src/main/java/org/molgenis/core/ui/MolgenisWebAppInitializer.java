package org.molgenis.core.ui;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import org.molgenis.core.ui.browserdetection.BrowserDetectionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class MolgenisWebAppInitializer {
  private static final int MB = 1024 * 1024;
  // the size threshold after which multi-part files will be written to disk.
  private static final int FILE_SIZE_THRESHOLD = 10 * MB;
  private static final Logger LOG = LoggerFactory.getLogger(MolgenisWebAppInitializer.class);

  @SuppressWarnings("unused")
  protected void onStartup(ServletContext servletContext, Class<?> appConfig) {
    // no maximum field size provided? default to 32 Mb
    onStartup(servletContext, appConfig, 32);
  }

  /** A Molgenis common web application initializer */
  protected void onStartup(ServletContext servletContext, Class<?> appConfig, int maxFileSize) {
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
      final long maxSize = (long) maxFileSize * MB;
      dispatcherServletRegistration.addMapping("/");
      dispatcherServletRegistration.setMultipartConfig(
          new MultipartConfigElement(null, maxSize, maxSize, FILE_SIZE_THRESHOLD));
      dispatcherServletRegistration.setAsyncSupported(true);
    }

    // add filters
    Dynamic forwardedHeaderFilter =
        servletContext.addFilter("forwardedHeaderFilter", ForwardedHeaderFilter.class);
    forwardedHeaderFilter.setAsyncSupported(true);
    forwardedHeaderFilter.addMappingForUrlPatterns(
        EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), false, "*");

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
