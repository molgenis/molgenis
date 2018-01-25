package org.molgenis.swagger.controller;

import org.mockito.Mock;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.i18n.LanguageService;
import org.molgenis.security.core.token.TokenService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

@SecurityTestExecutionListeners
public class SwaggerControllerTest extends AbstractTestNGSpringContextTests
{
	@Mock
	private MetaDataService metaDataService;
	@Mock
	private TokenService tokenService;
	@Mock
	private HttpServletResponse response;
	@Mock
	private EntityType type1;
	@Mock
	private EntityType type2;
	@Mock
	private Model model;

	private SwaggerController swaggerController;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		initMocks(this);
		reset(metaDataService, type1, type2, tokenService);
		swaggerController = new SwaggerController(metaDataService, tokenService);
	}

	@Test
	@WithMockUser
	public void testInit() throws Exception
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		when(tokenService.generateAndStoreToken("user", "For Swagger UI")).thenReturn("ABCDEFG");
		assertEquals("view-swagger-ui", swaggerController.init(model));
		verify(model).addAttribute("molgenisUrl", "http://localhost/plugin/swagger/swagger.yml");
		verify(model).addAttribute("baseUrl", "http://localhost");
		verify(model).addAttribute("token", "ABCDEFG");
		verifyNoMoreInteractions(model);
	}

	@Test
	public void testInitWithoutUser() throws Exception
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		assertEquals("view-swagger-ui", swaggerController.init(model));
		verify(model).addAttribute("molgenisUrl", "http://localhost/plugin/swagger/swagger.yml");
		verify(model).addAttribute("baseUrl", "http://localhost");
		verifyNoMoreInteractions(model);
	}

	@Test
	public void testSwagger()
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		when(type1.getId()).thenReturn("abc_EntityType2ëæ");
		when(type2.getId()).thenReturn("abc_EntityType1ëæ");
		when(metaDataService.getEntityTypes()).thenReturn(Stream.of(type1, type2));

		assertEquals("view-swagger", swaggerController.swagger(model, response));

		verify(model).addAttribute("scheme", "http");
		verify(model).addAttribute("host", "localhost");
		verify(model).addAttribute("entityTypes", newArrayList("abc_EntityType1ëæ", "abc_EntityType2ëæ"));
		verify(model).addAttribute("attributeTypes", AttributeType.getOptionsLowercase());
		verify(model).addAttribute("languageCodes", LanguageService.getLanguageCodes().collect(toList()));
		verifyNoMoreInteractions(model);
	}

	@Test
	public void testSwaggerHostAddsPort() throws URISyntaxException
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setScheme("http");
		request.setMethod("GET");
		request.setServletPath("/plugin/swagger/");
		request.setServerPort(8080);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		when(metaDataService.getEntityTypes()).thenReturn(Stream.empty());

		assertEquals("view-swagger", swaggerController.swagger(model, response));

		verify(model).addAttribute("scheme", "http");
		verify(model).addAttribute("host", "localhost:8080");
	}
}
