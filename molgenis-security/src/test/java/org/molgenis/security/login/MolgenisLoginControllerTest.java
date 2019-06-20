package org.molgenis.security.login;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.security.login.MolgenisLoginController.SPRING_SECURITY_CONTEXT;
import static org.molgenis.security.login.MolgenisLoginController.SPRING_SECURITY_SAVED_REQUEST;
import static org.molgenis.security.login.MolgenisLoginController.VIEW_LOGIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.molgenis.security.login.MolgenisLoginControllerTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = {Config.class})
public class MolgenisLoginControllerTest extends AbstractTestNGSpringContextTests {
  @Autowired private MolgenisLoginController molgenisLoginController;

  private MockMvc mockMvc;

  @BeforeMethod
  public void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(molgenisLoginController).build();
  }

  @Test
  public void getLoginPage() throws Exception {
    this.mockMvc
        .perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("view-login"));
  }

  @Test
  public void getLoginErrorPageSessionExpired() throws Exception {
    this.mockMvc
        .perform(get("/login").param("expired", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("view-login"))
        .andExpect(model().attribute("errorMessage", "Your login session has expired."));
  }

  @Test
  public void getLoginErrorPage() throws Exception {
    this.mockMvc
        .perform(get("/login").param("error", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("view-login"))
        .andExpect(model().attributeExists("errorMessage"));
  }

  @Test
  public void getLoginPageNoSession() throws Exception {
    MolgenisLoginController controller = new MolgenisLoginController();

    Model model = mock(Model.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getSession(false)).thenReturn(null);

    assertEquals(controller.getLoginPage(request, model), VIEW_LOGIN);
    verifyNoMoreInteractions(model);
  }

  @Test
  public void getLoginPageAuthenticated() throws Exception {
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
  public void getLoginPageExpired() throws Exception {
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
    verify(model).addAttribute("errorMessage", "Your login session has expired.");
  }

  @Configuration
  public static class Config extends WebMvcConfigurerAdapter {
    @Bean
    public MolgenisLoginController molgenisLoginController() {
      return new MolgenisLoginController();
    }
  }
}
