package org.molgenis.security;

import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.REQUEST;

import java.util.EnumSet;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.web.filter.ForwardedHeaderFilter;

public class WebAppSecurityInitializer extends AbstractSecurityWebApplicationInitializer {

  @Override
  protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
    // add filters
    Dynamic forwardedHeaderFilter =
        servletContext.addFilter("forwardedHeaderFilter", ForwardedHeaderFilter.class);
    forwardedHeaderFilter.setAsyncSupported(true);
    forwardedHeaderFilter.addMappingForUrlPatterns(EnumSet.of(REQUEST, ERROR, ASYNC), false, "*");
  }
}
