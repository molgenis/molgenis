package org.molgenis.security.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.security.login.MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE;
import static org.molgenis.security.login.MolgenisLoginController.ERROR_MESSAGE_SESSION_AUTHENTICATION;
import static org.molgenis.security.login.MolgenisLoginController.SPRING_SECURITY_CONTEXT;
import static org.molgenis.security.login.MolgenisLoginController.SPRING_SECURITY_SAVED_REQUEST;
import static org.molgenis.security.login.MolgenisLoginController.VIEW_LOGIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.security.login.MolgenisLoginControllerTest.Config;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@WebAppConfiguration
@ContextConfiguration(classes = {Config.class})
class MolgenisLoginControllerTest extends AbstractMockitoSpringContextTests {
  @Autowired private MolgenisLoginController molgenisLoginController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(molgenisLoginController).build();
  }

  @Test
  void getLoginPage() throws Exception {
    this.mockMvc
        .perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("view-login"));
  }

  @Test
  void getLoginErrorPageSessionExpired() throws Exception {
    this.mockMvc
        .perform(get("/login").param("expired", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("view-login"))
        .andExpect(model().attribute("errorMessage", "Your login session has expired."));
  }

  @Test
  void getLoginErrorPage() throws Exception {
    this.mockMvc
        .perform(get("/login").param("error", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("view-login"))
        .andExpect(model().attributeExists("errorMessage"));
  }

  @Test
  void getLoginPageNoSession() {
    MolgenisLoginController controller = new MolgenisLoginController();

    Model model = mock(Model.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getSession(false)).thenReturn(null);

    assertEquals(controller.getLoginPage(request, model), VIEW_LOGIN);
    verifyNoMoreInteractions(model);
  }

  @Test
  void getLoginPageAuthenticated() {
    MolgenisLoginController controller = new MolgenisLoginController();

    Model model = mock(Model.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    SavedRequest savedRequest = mock(SavedRequest.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    Cookie cookie = mock(Cookie.class);
    Authentication authentication = mock(Authentication.class);

    when(cookie.getName()).thenReturn("JSESSIONID");
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(savedRequest.getCookies()).thenReturn(Collections.singletonList(cookie));
    when(session.getAttribute(SPRING_SECURITY_SAVED_REQUEST)).thenReturn(savedRequest);
    when(session.getAttribute(SPRING_SECURITY_CONTEXT)).thenReturn(securityContext);
    when(request.getSession(false)).thenReturn(session);

    assertEquals(controller.getLoginPage(request, model), VIEW_LOGIN);
    verifyNoMoreInteractions(model);
  }

  @Test
  void getLoginPageExpired() {
    MolgenisLoginController controller = new MolgenisLoginController();

    Model model = mock(Model.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    SavedRequest savedRequest = mock(SavedRequest.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    Cookie cookie = mock(Cookie.class);

    when(cookie.getName()).thenReturn("JSESSIONID");
    when(savedRequest.getCookies()).thenReturn(Collections.singletonList(cookie));
    when(session.getAttribute(SPRING_SECURITY_SAVED_REQUEST)).thenReturn(savedRequest);
    when(session.getAttribute(SPRING_SECURITY_CONTEXT)).thenReturn(securityContext);
    when(request.getSession(false)).thenReturn(session);

    assertEquals(controller.getLoginPage(request, model), VIEW_LOGIN);
    verify(model).addAttribute(ERROR_MESSAGE_ATTRIBUTE, ERROR_MESSAGE_SESSION_AUTHENTICATION);
  }

  @Configuration
  static class Config extends WebMvcConfigurerAdapter {
    @Bean
    MolgenisLoginController molgenisLoginController() {
      return new MolgenisLoginController();
    }
  }
}
