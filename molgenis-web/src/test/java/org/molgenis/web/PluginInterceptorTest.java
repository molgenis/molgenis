package org.molgenis.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.web.PluginAttributes.KEY_AUTHENTICATED;
import static org.molgenis.web.PluginAttributes.KEY_CONTEXT_URL;
import static org.molgenis.web.PluginAttributes.KEY_PLUGIN_ID;
import static org.molgenis.web.PluginAttributes.KEY_PLUGIN_ID_WITH_QUERY_STRING;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.DataService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.model.Menu;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

class PluginInterceptorTest {
  private MenuReaderService menuReaderService;
  private UserPermissionEvaluator permissionService;
  private Authentication authentication;
  private SecurityContext previousContext;

  @BeforeEach
  void setUp() {
    menuReaderService = mock(MenuReaderService.class);
    when(menuReaderService.getMenu()).thenReturn(Optional.of(mock(Menu.class)));
    permissionService = mock(UserPermissionEvaluator.class);

    previousContext = SecurityContextHolder.getContext();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn("username");
    testContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(testContext);
  }

  @AfterEach
  void tearDownAfterMethod() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  void MolgenisPluginInterceptor() {
    assertThrows(NullPointerException.class, () -> new PluginInterceptor(null, null));
  }

  @Test
  void preHandle() {
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    PluginController molgenisPlugin = createMolgenisPluginController(uri);
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBean()).thenReturn(molgenisPlugin);

    PluginInterceptor molgenisPluginInterceptor =
        new PluginInterceptor(menuReaderService, permissionService);

    MockHttpServletRequest request = new MockHttpServletRequest();
    assertTrue(molgenisPluginInterceptor.preHandle(request, null, handlerMethod));
    assertEquals(uri, request.getAttribute(KEY_CONTEXT_URL));
  }

  @Test
  void preHandle_hasContextUrl() {
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
    assertEquals(contextUri, request.getAttribute(KEY_CONTEXT_URL));
  }

  @Test
  void postHandle() {
    PluginInterceptor molgenisPluginInterceptor =
        new PluginInterceptor(menuReaderService, permissionService);
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    ModelAndView modelAndView = new ModelAndView();
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));
    molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
    assertEquals("test", modelAndView.getModel().get(KEY_PLUGIN_ID));
    assertNotNull(modelAndView.getModel().get(PluginAttributes.KEY_MENU));
    assertEquals(false, modelAndView.getModel().get(KEY_AUTHENTICATED));
  }

  @Test
  void postHandle_pluginIdExists() {
    PluginInterceptor molgenisPluginInterceptor =
        new PluginInterceptor(menuReaderService, permissionService);
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject(PluginAttributes.KEY_PLUGIN_ID, "plugin_id");
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));
    molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
    assertEquals("plugin_id", modelAndView.getModel().get(KEY_PLUGIN_ID));
    assertNotNull(modelAndView.getModel().get(PluginAttributes.KEY_MENU));
    assertEquals(false, modelAndView.getModel().get(KEY_AUTHENTICATED));
  }

  @Test
  void postHandlePluginidWithQueryString() {
    PluginInterceptor molgenisPluginInterceptor =
        new PluginInterceptor(menuReaderService, permissionService);
    String uri = PluginController.PLUGIN_URI_PREFIX + "plugin_id_test";

    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    when(mockHttpServletRequest.getQueryString()).thenReturn("entity=entityTypeId");

    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));

    ModelAndView modelAndView = new ModelAndView();
    molgenisPluginInterceptor.postHandle(mockHttpServletRequest, null, handlerMethod, modelAndView);
    assertEquals("plugin_id_test", modelAndView.getModel().get(KEY_PLUGIN_ID));
    assertNotNull(modelAndView.getModel().get(PluginAttributes.KEY_MENU));
    assertEquals(false, modelAndView.getModel().get(KEY_AUTHENTICATED));
    assertEquals(
        "plugin_id_test?entity=entityTypeId",
        modelAndView.getModel().get(KEY_PLUGIN_ID_WITH_QUERY_STRING));
  }

  @Test
  void postHandle_authenticated() {
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
    assertEquals("test", modelAndView.getModel().get(KEY_PLUGIN_ID));
    assertNotNull(modelAndView.getModel().get(PluginAttributes.KEY_MENU));
    assertEquals(isAuthenticated, modelAndView.getModel().get(KEY_AUTHENTICATED));
  }

  @Test
  void postHandle_notAuthenticated() {
    boolean isAuthenticated = false;
    when(authentication.isAuthenticated()).thenReturn(isAuthenticated);

    PluginInterceptor molgenisPluginInterceptor =
        new PluginInterceptor(menuReaderService, permissionService);
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    ModelAndView modelAndView = new ModelAndView();
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));
    molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
    assertEquals("test", modelAndView.getModel().get(KEY_PLUGIN_ID));
    assertNotNull(modelAndView.getModel().get(PluginAttributes.KEY_MENU));
    assertEquals(isAuthenticated, modelAndView.getModel().get(KEY_AUTHENTICATED));
  }

  private PluginController createMolgenisPluginController(String uri) {
    PluginController pluginController = new PluginController(uri) {};
    DataService dataService = mock(DataService.class);
    pluginController.setDataService(dataService);
    return pluginController;
  }
}
