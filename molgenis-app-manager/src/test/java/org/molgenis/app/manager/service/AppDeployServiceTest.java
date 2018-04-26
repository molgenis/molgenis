package org.molgenis.app.manager.service;

import org.molgenis.app.manager.service.impl.AppDeployServiceImpl;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

public class AppDeployServiceTest
{
	private AppDeployService appDeployService;

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);
		appDeployService = new AppDeployServiceImpl();
	}

	@Test
	public void testLoadJavascriptResource() throws IOException
	{
		ClassLoader classLoader = getClass().getClassLoader();

		MockHttpServletResponse response = new MockHttpServletResponse();
		appDeployService.loadResource(classLoader.getResource("test-resources").getFile() + "/js/test.js", response);

		assertEquals(response.getContentAsString(), "var test = \"test file\"");
		assertEquals(response.getContentType(), "application/javascript;charset=UTF-8");
		assertEquals(response.getContentLength(), 22);
		assertEquals(response.getHeader("Content-Disposition"), "attachment; filename=test.js");
	}

	@Test
	public void testLoadCSSResource() throws IOException
	{
		ClassLoader classLoader = getClass().getClassLoader();

		MockHttpServletResponse response = new MockHttpServletResponse();
		appDeployService.loadResource(classLoader.getResource("test-resources").getFile() + "/css/test.css", response);

		assertEquals(response.getContentAsString(), ".test {\n    color: red\n}");
		assertEquals(response.getContentType(), "text/css;charset=UTF-8");
		assertEquals(response.getContentLength(), 24);
		assertEquals(response.getHeader("Content-Disposition"), "attachment; filename=test.css");
	}

	@Test
	public void testLoadImageResource() throws IOException
	{
		ClassLoader classLoader = getClass().getClassLoader();

		MockHttpServletResponse response = new MockHttpServletResponse();
		appDeployService.loadResource(classLoader.getResource("test-resources").getFile() + "/img/test.png", response);

		assertEquals(response.getContentType(), "image/png");
		assertEquals(response.getContentLength(), 3725);
		assertEquals(response.getHeader("Content-Disposition"), "attachment; filename=test.png");
	}
}
