package org.molgenis.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisPluginInterceptorTest
{
	private Login login;
	private MolgenisPermissionService permissionService;
	private MolgenisUi molgenisUi;

	@BeforeMethod
	public void setUp()
	{
		login = mock(Login.class);
		permissionService = mock(MolgenisPermissionService.class);
		molgenisUi = mock(MolgenisUi.class);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MolgenisPluginInterceptor()
	{
		new MolgenisPluginInterceptor(null, null, null);
	}

	@Test
	public void preHandle() throws Exception
	{
		String uri = MolgenisPlugin.PLUGIN_URI_PREFIX + "test";
		MolgenisPlugin molgenisPlugin = new MolgenisPlugin(uri)
		{
		};
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(molgenisPlugin);
		MolgenisPluginInterceptor molgenisPluginInterceptor = new MolgenisPluginInterceptor(login, permissionService,
				molgenisUi);

		when(permissionService.hasPermissionOnPlugin(molgenisPlugin.getClass(), Permission.READ)).thenReturn(true);
		assertTrue(molgenisPluginInterceptor.preHandle(mock(HttpServletRequest.class), null, handlerMethod));
	}

	@Test(expectedExceptions = DatabaseAccessException.class)
	public void preHandle_accessDenied() throws Exception
	{
		String uri = MolgenisPlugin.PLUGIN_URI_PREFIX + "test";
		MolgenisPlugin molgenisPlugin = new MolgenisPlugin(uri)
		{
		};
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(molgenisPlugin);
		MolgenisPluginInterceptor molgenisPluginInterceptor = new MolgenisPluginInterceptor(login, permissionService,
				molgenisUi);

		when(permissionService.hasPermissionOnPlugin(molgenisPlugin.getClass(), Permission.READ)).thenReturn(false);
		molgenisPluginInterceptor.preHandle(mock(HttpServletRequest.class), null, handlerMethod);
	}

	@Test
	public void postHandle() throws Exception
	{
		MolgenisPluginInterceptor molgenisPluginInterceptor = new MolgenisPluginInterceptor(login, permissionService,
				molgenisUi);
		String uri = MolgenisPlugin.PLUGIN_URI_PREFIX + "test";
		ModelAndView modelAndView = new ModelAndView();
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(new MolgenisPlugin(uri)
		{
		});
		molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_PLUGIN_ID), "test");
		assertNotNull(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_MOLGENIS_UI));
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_AUTHENTICATED), false);
	}

	@Test
	public void postHandle_authenticated() throws Exception
	{
		when(login.isAuthenticated()).thenReturn(true);
		MolgenisPluginInterceptor molgenisPluginInterceptor = new MolgenisPluginInterceptor(login, permissionService,
				molgenisUi);
		String uri = MolgenisPlugin.PLUGIN_URI_PREFIX + "test";
		ModelAndView modelAndView = new ModelAndView();
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(new MolgenisPlugin(uri)
		{
		});
		molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_PLUGIN_ID), "test");
		assertNotNull(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_MOLGENIS_UI));
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_AUTHENTICATED), true);
	}
}
