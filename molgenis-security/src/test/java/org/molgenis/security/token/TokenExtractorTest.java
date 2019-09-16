package org.molgenis.security.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.molgenis.security.token.TokenExtractor.TOKEN_HEADER;
import static org.molgenis.security.token.TokenExtractor.TOKEN_PARAMETER;
import static org.molgenis.security.token.TokenExtractor.getToken;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.NativeWebRequest;

class TokenExtractorTest extends AbstractMockitoTest {
  private static final String TEST_TOKEN = "thetoken";
  private TokenExtractor tokenExtractor = new TokenExtractor();

  @Mock private MethodParameter methodParameter;

  @Mock private NativeWebRequest webRequest;

  @Mock private HttpServletRequest request;

  @Mock private TokenParam tokenParameter;

  @Test
  void testGetTokenFromHeader() {
    when(request.getHeader(TOKEN_HEADER)).thenReturn(TEST_TOKEN);
    assertEquals(TEST_TOKEN, getToken(request));
  }

  @Test
  void testGetTokenFromParam() {
    when(request.getParameter(TOKEN_PARAMETER)).thenReturn(TEST_TOKEN);
    assertEquals(TEST_TOKEN, getToken(request));
  }

  @Test
  void testResolveArgumentTokenInHeader() throws Exception {
    when(methodParameter.getParameterAnnotation(TokenParam.class)).thenReturn(tokenParameter);
    when(webRequest.getHeader(TOKEN_HEADER)).thenReturn(TEST_TOKEN);
    assertEquals(
        TEST_TOKEN,
        (String) tokenExtractor.resolveArgument(methodParameter, null, webRequest, null));
  }

  @Test
  void testResolveArgumentTokenInParam() throws Exception {
    when(methodParameter.getParameterAnnotation(TokenParam.class)).thenReturn(tokenParameter);
    when(webRequest.getParameter(TOKEN_PARAMETER)).thenReturn(TEST_TOKEN);
    assertEquals(
        TEST_TOKEN,
        (String) tokenExtractor.resolveArgument(methodParameter, null, webRequest, null));
  }

  @Test
  void testResolveArgumentNoToken() throws Exception {
    when(methodParameter.getParameterAnnotation(TokenParam.class)).thenReturn(tokenParameter);
    assertNull(tokenExtractor.resolveArgument(methodParameter, null, webRequest, null));
  }

  @Test
  void testResolveArgumentNoTokenRequired() throws Exception {
    when(methodParameter.getParameterAnnotation(TokenParam.class)).thenReturn(tokenParameter);
    when(tokenParameter.required()).thenReturn(true);
    Exception exception =
        assertThrows(
            ServletRequestBindingException.class,
            () -> tokenExtractor.resolveArgument(methodParameter, null, webRequest, null));
    assertThat(exception.getMessage())
        .containsPattern(
            "Missing molgenis token\\. Token should either be present in the x-molgenis-token request header or the molgenis-token parameter\\.");
  }
}
