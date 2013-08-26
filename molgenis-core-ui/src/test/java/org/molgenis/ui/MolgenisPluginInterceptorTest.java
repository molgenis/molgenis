package org.molgenis.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.molgenis.framework.security.Login;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisPluginInterceptorTest
{
	private Login login;
	private MolgenisUi molgenisUi;

	@BeforeMethod
	public void setUp()
	{
		login = mock(Login.class);
		molgenisUi = mock(MolgenisUi.class);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MolgenisPluginInterceptor()
	{
		new MolgenisPluginInterceptor(null, null);
	}

	@Test
	public void postHandle() throws Exception
	{
		MolgenisPluginInterceptor molgenisPluginInterceptor = new MolgenisPluginInterceptor(login, molgenisUi);
		String uri = MolgenisPluginController.PLUGIN_URI_PREFIX + "test";
		ModelAndView modelAndView = new ModelAndView();
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(new MolgenisPluginController(uri)
		{
		});
		molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(MolgenisPluginInterceptor.KEY_PLUGIN_ID), "test");
		assertNotNull(modelAndView.getModel().get(MolgenisPluginInterceptor.KEY_MOLGENIS_UI));
		assertEquals(modelAndView.getModel().get(MolgenisPluginInterceptor.KEY_AUTHENTICATED), false);
	}

	@Test
	public void postHandle_authenticated() throws Exception
	{
		when(login.isAuthenticated()).thenReturn(true);
		MolgenisPluginInterceptor molgenisPluginInterceptor = new MolgenisPluginInterceptor(login, molgenisUi);
		String uri = MolgenisPluginController.PLUGIN_URI_PREFIX + "test";
		ModelAndView modelAndView = new ModelAndView();
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.getBean()).thenReturn(new MolgenisPluginController(uri)
		{
		});
		molgenisPluginInterceptor.postHandle(null, null, handlerMethod, modelAndView);
		assertEquals(modelAndView.getModel().get(MolgenisPluginInterceptor.KEY_PLUGIN_ID), "test");
		assertNotNull(modelAndView.getModel().get(MolgenisPluginInterceptor.KEY_MOLGENIS_UI));
		assertEquals(modelAndView.getModel().get(MolgenisPluginInterceptor.KEY_AUTHENTICATED), true);
	}
}
