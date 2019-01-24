package org.molgenis.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.molgenis.data.DataService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.model.Menu;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PluginInterceptorTest {
  private MenuReaderService menuReaderService;
  private UserPermissionEvaluator permissionService;
  private Authentication authentication;

  @BeforeMethod
  public void setUp() {
    menuReaderService = mock(MenuReaderService.class);
    when(menuReaderService.getMenu()).thenReturn(Optional.of(mock(Menu.class)));
    permissionService = mock(UserPermissionEvaluator.class);
    authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn("username");
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void MolgenisPluginInterceptor() {
    new PluginInterceptor(null, null);
  }

  @Test
  public void preHandle() {
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    PluginController molgenisPlugin = createMolgenisPluginController(uri);
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBean()).thenReturn(molgenisPlugin);

    PluginInterceptor molgenisPluginInterceptor =
        new PluginInterceptor(menuReaderService, permissionService);

    MockHttpServletRequest request = new MockHttpServletRequest();
    assertTrue(molgenisPluginInterceptor.preHandle(request, null, handlerMethod));
    assertEquals(request.getAttribute(PluginAttributes.KEY_CONTEXT_URL), uri);
  }

  @Test
  public void preHandle_hasContextUrl() {
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    PluginController molgenisPlugin = createMolgenisPluginController(uri);
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBean()).thenReturn(molgenisPlugin);

    PluginInterceptor molgenisPluginInterceptor =
        new PluginInterceptor(menuReaderService, permissionService);

    String contextUri = "/plugin/not-test";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(PluginAttributes.KEY_CONTEXT_URL, contextUri);
    assertTrue(molgenisPluginInterceptor.preHandle(request, null, handlerMethod));
    assertEquals(request.getAttribute(PluginAttributes.KEY_CONTEXT_URL), contextUri);
  }

  @Test
  public void postHandle() {
    PluginInterceptor molgenisPluginInterceptor =
        new PluginInterceptor(menuReaderService, permissionService);
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    ModelAndView modelAndView = new ModelAndView();
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));
    molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
    assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_PLUGIN_ID), "test");
    assertNotNull(modelAndView.getModel().get(PluginAttributes.KEY_MENU));
    assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_AUTHENTICATED), false);
  }

  @Test
  public void postHandle_pluginIdExists() {
    PluginInterceptor molgenisPluginInterceptor =
        new PluginInterceptor(menuReaderService, permissionService);
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject(PluginAttributes.KEY_PLUGIN_ID, "plugin_id");
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));
    molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
    assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_PLUGIN_ID), "plugin_id");
    assertNotNull(modelAndView.getModel().get(PluginAttributes.KEY_MENU));
    assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_AUTHENTICATED), false);
  }

  @Test
  public void postHandlePluginidWithQueryString() {
    PluginInterceptor molgenisPluginInterceptor =
        new PluginInterceptor(menuReaderService, permissionService);
    String uri = PluginController.PLUGIN_URI_PREFIX + "plugin_id_test";

    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    when(mockHttpServletRequest.getQueryString()).thenReturn("entity=entityTypeId");

    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));

    ModelAndView modelAndView = new ModelAndView();
    molgenisPluginInterceptor.postHandle(mockHttpServletRequest, null, handlerMethod, modelAndView);
    assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_PLUGIN_ID), "plugin_id_test");
    assertNotNull(modelAndView.getModel().get(PluginAttributes.KEY_MENU));
    assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_AUTHENTICATED), false);
    assertEquals(
        modelAndView.getModel().get(PluginAttributes.KEY_PLUGIN_ID_WITH_QUERY_STRING),
        "plugin_id_test?entity=entityTypeId");
  }

  @Test
  public void postHandle_authenticated() {
    boolean isAuthenticated = true;
    when(authentication.isAuthenticated()).thenReturn(isAuthenticated);

    PluginInterceptor molgenisPluginInterceptor =
        new PluginInterceptor(menuReaderService, permissionService);
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    ModelAndView modelAndView = new ModelAndView();
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    PluginController pluginController = createMolgenisPluginController(uri);
    DataService dataService = mock(DataService.class);
    pluginController.setDataService(dataService);
    when(handlerMethod.getBean()).thenReturn(pluginController);
    molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
    assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_PLUGIN_ID), "test");
    assertNotNull(modelAndView.getModel().get(PluginAttributes.KEY_MENU));
    assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_AUTHENTICATED), isAuthenticated);
  }

  @Test
  public void postHandle_notAuthenticated() {
    boolean isAuthenticated = false;
    when(authentication.isAuthenticated()).thenReturn(isAuthenticated);

    PluginInterceptor molgenisPluginInterceptor =
        new PluginInterceptor(menuReaderService, permissionService);
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    ModelAndView modelAndView = new ModelAndView();
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));
    molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
    assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_PLUGIN_ID), "test");
    assertNotNull(modelAndView.getModel().get(PluginAttributes.KEY_MENU));
    assertEquals(modelAndView.getModel().get(PluginAttributes.KEY_AUTHENTICATED), isAuthenticated);
  }

  private PluginController createMolgenisPluginController(String uri) {
    PluginController pluginController = new PluginController(uri) {};
    DataService dataService = mock(DataService.class);
    pluginController.setDataService(dataService);
    return pluginController;
  }
}
