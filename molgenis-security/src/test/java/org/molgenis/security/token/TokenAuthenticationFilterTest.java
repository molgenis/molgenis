package org.molgenis.security.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import java.io.IOException;
import java.util.Arrays;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class TokenAuthenticationFilterTest {
  private TokenAuthenticationFilter filter;
  private AuthenticationProvider authenticationProvider;

  @BeforeEach
  void beforeMethod() {
    authenticationProvider = mock(AuthenticationProvider.class);
    filter = new TokenAuthenticationFilter(authenticationProvider);
  }

  @Test
  void doFilter() throws IOException, ServletException {
    SecurityContext previous = SecurityContextHolder.getContext();
    try {
      SecurityContext testContext = SecurityContextHolder.createEmptyContext();
      SecurityContextHolder.setContext(testContext);

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

      assertEquals(auth, getContext().getAuthentication());
    } finally {
      SecurityContextHolder.setContext(previous);
    }
  }
}
