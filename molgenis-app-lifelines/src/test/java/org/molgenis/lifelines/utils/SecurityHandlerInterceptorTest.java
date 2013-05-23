package org.molgenis.lifelines.utils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.security.SimpleLogin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.lifelines.plugins.CatalogueLoaderPlugin;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SecurityHandlerInterceptorTest
{
	private SecurityHandlerInterceptor securityHandlerInterceptor;
	private MockHttpServletRequest httpServletRequest;
	private MockHttpServletResponse httpServletResponse;

	@BeforeMethod
	public void setUp()
	{
		securityHandlerInterceptor = new SecurityHandlerInterceptor(CatalogueLoaderPlugin.class);
		httpServletRequest = new MockHttpServletRequest();
		httpServletResponse = new MockHttpServletResponse();
	}

	@Test
	public void preHandleAthorizedLogin() throws Exception
	{
		securityHandlerInterceptor.setLogin(new SimpleLogin()
		{

			@Override
			public boolean canReadScreenController(Class<? extends ScreenController<?>> screenControllerClass)
					throws DatabaseException
			{
				return true;
			}

		});
		boolean result = securityHandlerInterceptor.preHandle(httpServletRequest, httpServletResponse, null);
		assertTrue(result);
		assertEquals(httpServletResponse.getStatus(), HttpServletResponse.SC_OK);
	}

	@Test
	public void preHandleNoLogin() throws Exception
	{
		boolean result = securityHandlerInterceptor.preHandle(httpServletRequest, httpServletResponse, null);
		assertFalse(result);
		assertEquals(httpServletResponse.getStatus(), HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Test
	public void preHandleNotAthorizedLogin() throws Exception
	{
		securityHandlerInterceptor.setLogin(new SimpleLogin()
		{

			@Override
			public boolean canReadScreenController(Class<? extends ScreenController<?>> screenControllerClass)
					throws DatabaseException
			{
				return false;
			}

		});
		boolean result = securityHandlerInterceptor.preHandle(httpServletRequest, httpServletResponse, null);
		assertFalse(result);
		assertEquals(httpServletResponse.getStatus(), HttpServletResponse.SC_UNAUTHORIZED);
	}
}
