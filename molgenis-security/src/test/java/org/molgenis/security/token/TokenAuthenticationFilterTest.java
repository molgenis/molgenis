package org.molgenis.security.token;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TokenAuthenticationFilterTest {
  private SecurityContext securityContext;
  private TokenAuthenticationFilter filter;
  private AuthenticationProvider authenticationProvider;

  @BeforeMethod
  public void beforeMethod() {
    authenticationProvider = mock(AuthenticationProvider.class);
    filter = new TokenAuthenticationFilter(authenticationProvider);
    securityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);
  }

  @DataProvider(name = "requestUriProvider")
  public static Iterator<Object[]> parseProvider() {
    return newArrayList(
            new Object[] {"/login"},
            new Object[] {"/logout"},
            new Object[] {"/api/login"},
            new Object[] {"/api/logout"})
        .iterator();
  }

  @Test(dataProvider = "requestUriProvider")
  public void doFilterLoginRequestUri(String requestUri) throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(requestUri);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    verifyZeroInteractions(authenticationProvider);
  }

  @Test
  public void doFilterIgnoreInvalidToken() throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/some/uri");
    request.addHeader(TokenExtractor.TOKEN_HEADER, "invalid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    Authentication authentication = mock(Authentication.class);
    when(authenticationProvider.authenticate(new RestAuthenticationToken("invalid-token")))
        .thenReturn(authentication);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    verifyZeroInteractions(securityContext);
  }

  @Test
  public void doFilterAuthenticationException() throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/some/uri");
    request.addHeader(TokenExtractor.TOKEN_HEADER, "token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    AuthenticationException authenticationException = mock(AuthenticationException.class);
    doThrow(authenticationException)
        .when(authenticationProvider)
        .authenticate(new RestAuthenticationToken("token"));

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    verifyZeroInteractions(securityContext);
  }

  @Test
  public void doFilterIgnoreBlankToken() throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/some/uri");
    request.addHeader(TokenExtractor.TOKEN_HEADER, " ");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    verifyZeroInteractions(securityContext);
  }

  @Test
  public void doFilter() throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    RestAuthenticationToken auth =
        new RestAuthenticationToken(
            "admin", "admin", Arrays.asList(new SimpleGrantedAuthority("admin")), "token");

    request.setRequestURI("/api/v1/dataset");
    request.addHeader(TokenExtractor.TOKEN_HEADER, "token");
    when(authenticationProvider.authenticate(new RestAuthenticationToken("token")))
        .thenReturn(auth);

    filter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
    // original context set after filtering
    assertEquals(SecurityContextHolder.getContext(), securityContext);
  }
}
