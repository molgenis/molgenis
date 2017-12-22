package org.molgenis.security.token;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.NativeWebRequest;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.when;
import static org.molgenis.security.token.TokenExtractor.TOKEN_HEADER;
import static org.molgenis.security.token.TokenExtractor.TOKEN_PARAMETER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class TokenExtractorTest extends AbstractMockitoTest
{
	private static final String TEST_TOKEN = "thetoken";
	private TokenExtractor tokenExtractor = new TokenExtractor();

	@Mock
	private MethodParameter methodParameter;

	@Mock
	private NativeWebRequest webRequest;

	@Mock
	private HttpServletRequest request;

	@Mock
	private TokenParam tokenParameter;

	@Test
	public void testGetTokenFromHeader()
	{
		when(request.getHeader(TOKEN_HEADER)).thenReturn(TEST_TOKEN);
		assertEquals(TokenExtractor.getToken(request), TEST_TOKEN);
	}

	@Test
	public void testGetTokenFromParam()
	{
		when(request.getParameter(TOKEN_PARAMETER)).thenReturn(TEST_TOKEN);
		assertEquals(TokenExtractor.getToken(request), TEST_TOKEN);
	}

	@Test
	public void testResolveArgumentTokenInHeader() throws ServletRequestBindingException
	{
		when(methodParameter.getParameterAnnotation(TokenParam.class)).thenReturn(tokenParameter);
		when(webRequest.getHeader(TOKEN_HEADER)).thenReturn(TEST_TOKEN);
		assertEquals((String) tokenExtractor.resolveArgument(methodParameter, null, webRequest, null), TEST_TOKEN);
	}

	@Test
	public void testResolveArgumentTokenInParam() throws ServletRequestBindingException
	{
		when(methodParameter.getParameterAnnotation(TokenParam.class)).thenReturn(tokenParameter);
		when(webRequest.getParameter(TOKEN_PARAMETER)).thenReturn(TEST_TOKEN);
		assertEquals((String) tokenExtractor.resolveArgument(methodParameter, null, webRequest, null), TEST_TOKEN);
	}

	@Test
	public void testResolveArgumentNoToken() throws ServletRequestBindingException
	{
		when(methodParameter.getParameterAnnotation(TokenParam.class)).thenReturn(tokenParameter);
		assertNull(tokenExtractor.resolveArgument(methodParameter, null, webRequest, null));
	}

	@Test(expectedExceptions = ServletRequestBindingException.class, expectedExceptionsMessageRegExp = "Missing molgenis token\\. Token should either be present in the x-molgenis-token request header or the molgenis-token parameter\\.")
	public void testResolveArgumentNoTokenRequired() throws ServletRequestBindingException
	{
		when(methodParameter.getParameterAnnotation(TokenParam.class)).thenReturn(tokenParameter);
		when(tokenParameter.required()).thenReturn(true);
		tokenExtractor.resolveArgument(methodParameter, null, webRequest, null);
	}

}
