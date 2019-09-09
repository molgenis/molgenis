package org.molgenis.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.account.AccountController.CHANGE_PASSWORD_URI;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SecurityTestExecutionListeners
class MolgenisChangePasswordFilterTest {
  @Mock private UserService userService;
  private MolgenisChangePasswordFilter filter;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private FilterChain chain;

  @BeforeEach
  void setUpBeforeMethod() {
    filter = new MolgenisChangePasswordFilter(userService, new DefaultRedirectStrategy());
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    chain = mock(FilterChain.class);
  }

  @Test
  @WithMockUser(username = "user")
  void testDoFilterChangePassword() throws IOException, ServletException {
    User user = mock(User.class);
    when(user.isChangePassword()).thenReturn(true);
    when(userService.getUser("user")).thenReturn(user);

    request.setRequestURI("/login");

    filter.doFilter(request, response, chain);

    assertEquals(response.getRedirectedUrl(), CHANGE_PASSWORD_URI);
  }

  @Test
  @WithMockUser(username = "user")
  void testDoFilterNoChangePassword() throws IOException, ServletException {
    User user = mock(User.class);
    when(userService.getUser("user")).thenReturn(user);

    request.setRequestURI("/login");

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  @WithMockUser(username = "user")
  void testDoFilterIgnoreOwnUri() throws IOException, ServletException {
    request.setRequestURI("/account/password/change");

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  @WithMockUser(username = "user")
  void testDoFilterChangePasswordHackyUri() throws IOException, ServletException {
    User user = mock(User.class);
    when(user.isChangePassword()).thenReturn(true);
    when(userService.getUser("user")).thenReturn(user);

    request.setRequestURI("/api/v2/account/password/change");

    filter.doFilter(request, response, chain);

    assertEquals(response.getRedirectedUrl(), CHANGE_PASSWORD_URI);
  }
}
