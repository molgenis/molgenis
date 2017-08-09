package org.molgenis.ui;

import org.molgenis.data.DataService;
import org.molgenis.security.core.MolgenisPermissionService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class MolgenisPluginInterceptorTest
{
	private MolgenisUi molgenisUi;
	private MolgenisPermissionService molgenisPermissionService;
	private Authentication authentication;

	@BeforeMethod
	public void setUp()
	{
		molgenisUi = mock(MolgenisUi.class);
		molgenisPermissionService = mock(MolgenisPermissionService.class);
		authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn("username");
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void MolgenisPluginInterceptor()
	{
		new MolgenisPluginInterceptor(null, null);
	}

	@Test
	public void preHandle() throws Exception
	{
		String uri = MolgenisPluginController.PLUGIN_URI_PREFIX + "test";
		MolgenisPluginController molgenisPlugin = createMolgenisPluginController(uri);
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(molgenisPlugin);

		MolgenisPluginInterceptor molgenisPluginInterceptor = new MolgenisPluginInterceptor(molgenisUi,
				molgenisPermissionService);

		MockHttpServletRequest request = new MockHttpServletRequest();
		assertTrue(molgenisPluginInterceptor.preHandle(request, null, handlerMethod));
		assertEquals(request.getAttribute(MolgenisPluginAttributes.KEY_CONTEXT_URL), uri);
	}

	@Test
	public void preHandle_hasContextUrl() throws Exception
	{
		String uri = MolgenisPluginController.PLUGIN_URI_PREFIX + "test";
		MolgenisPluginController molgenisPlugin = createMolgenisPluginController(uri);
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(molgenisPlugin);

		MolgenisPluginInterceptor molgenisPluginInterceptor = new MolgenisPluginInterceptor(molgenisUi,
				molgenisPermissionService);

		String contextUri = "/plugin/not-test";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute(MolgenisPluginAttributes.KEY_CONTEXT_URL, contextUri);
		assertTrue(molgenisPluginInterceptor.preHandle(request, null, handlerMethod));
		assertEquals(request.getAttribute(MolgenisPluginAttributes.KEY_CONTEXT_URL), contextUri);

	}

	@Test
	public void postHandle() throws Exception
	{
		MolgenisPluginInterceptor molgenisPluginInterceptor = new MolgenisPluginInterceptor(molgenisUi,
				molgenisPermissionService);
		String uri = MolgenisPluginController.PLUGIN_URI_PREFIX + "test";
		ModelAndView modelAndView = new ModelAndView();
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));
		molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_PLUGIN_ID), "test");
		assertNotNull(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_MOLGENIS_UI));
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_AUTHENTICATED), false);
	}

	@Test
	public void postHandle_pluginIdExists() throws Exception
	{
		MolgenisPluginInterceptor molgenisPluginInterceptor = new MolgenisPluginInterceptor(molgenisUi,
				molgenisPermissionService);
		String uri = MolgenisPluginController.PLUGIN_URI_PREFIX + "test";
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject(MolgenisPluginAttributes.KEY_PLUGIN_ID, "plugin_id");
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));
		molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_PLUGIN_ID), "plugin_id");
		assertNotNull(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_MOLGENIS_UI));
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_AUTHENTICATED), false);
	}

	@Test
	public void postHandlePluginidWithQueryString() throws Exception
	{
		MolgenisPluginInterceptor molgenisPluginInterceptor = new MolgenisPluginInterceptor(molgenisUi,
				molgenisPermissionService);
		String uri = MolgenisPluginController.PLUGIN_URI_PREFIX + "plugin_id_test";

		HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
		when(mockHttpServletRequest.getQueryString()).thenReturn("entity=entityTypeId");

		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));

		ModelAndView modelAndView = new ModelAndView();
		molgenisPluginInterceptor.postHandle(mockHttpServletRequest, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_PLUGIN_ID), "plugin_id_test");
		assertNotNull(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_MOLGENIS_UI));
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_AUTHENTICATED), false);
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_PLUGIN_ID_WITH_QUERY_STRING),
				"plugin_id_test?entity=entityTypeId");
	}

	@Test
	public void postHandle_authenticated() throws Exception
	{
		boolean isAuthenticated = true;
		when(authentication.isAuthenticated()).thenReturn(isAuthenticated);

		MolgenisPluginInterceptor molgenisPluginInterceptor = new MolgenisPluginInterceptor(molgenisUi,
				molgenisPermissionService);
		String uri = MolgenisPluginController.PLUGIN_URI_PREFIX + "test";
		ModelAndView modelAndView = new ModelAndView();
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		MolgenisPluginController pluginController = createMolgenisPluginController(uri);
		DataService dataService = mock(DataService.class);
		pluginController.setDataService(dataService);
		when(handlerMethod.getBean()).thenReturn(pluginController);
		molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_PLUGIN_ID), "test");
		assertNotNull(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_MOLGENIS_UI));
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_AUTHENTICATED), isAuthenticated);
	}

	@Test
	public void postHandle_notAuthenticated() throws Exception
	{
		boolean isAuthenticated = false;
		when(authentication.isAuthenticated()).thenReturn(isAuthenticated);

		MolgenisPluginInterceptor molgenisPluginInterceptor = new MolgenisPluginInterceptor(molgenisUi,
				molgenisPermissionService);
		String uri = MolgenisPluginController.PLUGIN_URI_PREFIX + "test";
		ModelAndView modelAndView = new ModelAndView();
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(createMolgenisPluginController(uri));
		molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_PLUGIN_ID), "test");
		assertNotNull(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_MOLGENIS_UI));
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_AUTHENTICATED), isAuthenticated);
	}

	private MolgenisPluginController createMolgenisPluginController(String uri)
	{
		MolgenisPluginController pluginController = new MolgenisPluginController(uri)
		{
		};
		DataService dataService = mock(DataService.class);
		pluginController.setDataService(dataService);
		return pluginController;
	}
}
