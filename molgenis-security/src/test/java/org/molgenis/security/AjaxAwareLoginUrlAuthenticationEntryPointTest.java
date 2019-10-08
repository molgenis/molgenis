package org.molgenis.security;

import static java.util.Collections.enumeration;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.OPTIONS;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;

class AjaxAwareLoginUrlAuthenticationEntryPointTest {
  private AjaxAwareLoginUrlAuthenticationEntryPoint ajaxAwareLoginUrlAuthenticationEntryPoint;

  @BeforeEach
  void setUpBeforeMethod() {
    ajaxAwareLoginUrlAuthenticationEntryPoint =
        new AjaxAwareLoginUrlAuthenticationEntryPoint("/login");
  }

  @Test
  void testCommencePreflight() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn(OPTIONS.toString()).getMock();
    HttpServletResponse response = mock(HttpServletResponse.class);
    AuthenticationException authException = mock(AuthenticationException.class);
    ajaxAwareLoginUrlAuthenticationEntryPoint.commence(request, response, authException);
    verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  @Test
  void testCommenceRest() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    Enumeration<String> headerValueEnumeration = enumeration(singleton("XMLHttpRequest"));
    when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
    when(request.getHeaders("X-Requested-With")).thenReturn(headerValueEnumeration);
    HttpServletResponse response = mock(HttpServletResponse.class);
    AuthenticationException authException = mock(AuthenticationException.class);
    ajaxAwareLoginUrlAuthenticationEntryPoint.commence(request, response, authException);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  void testCommenceOther() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("molgenis.org");
    when(request.getServerPort()).thenReturn(80);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.encodeRedirectURL("http://molgenis.org/login"))
        .thenReturn("http://molgenis.org/login");
    AuthenticationException authException = mock(AuthenticationException.class);
    ajaxAwareLoginUrlAuthenticationEntryPoint.commence(request, response, authException);
    verify(response).sendRedirect("http://molgenis.org/login");
  }
}
