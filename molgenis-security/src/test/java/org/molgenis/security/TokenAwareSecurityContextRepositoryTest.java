package org.molgenis.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TokenAwareSecurityContextRepositoryTest extends AbstractMockitoTest {
  @Mock private SecurityContextRepository tokenSecurityContextRepository;
  @Mock private SecurityContextRepository defaultSecurityContextRepository;
  private TokenAwareSecurityContextRepository tokenAwareSecurityContextRepository;

  @BeforeMethod
  public void setUpBeforeMethod() {
    tokenAwareSecurityContextRepository =
        new TokenAwareSecurityContextRepository(
            tokenSecurityContextRepository, defaultSecurityContextRepository);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testTokenAwareSecurityContextRepository() {
    new TokenAwareSecurityContextRepository(null, null);
  }

  @Test
  public void testLoadContext() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    HttpRequestResponseHolder holder = new HttpRequestResponseHolder(request, response);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(defaultSecurityContextRepository.loadContext(holder)).thenReturn(securityContext);
    assertEquals(tokenAwareSecurityContextRepository.loadContext(holder), securityContext);
  }

  @Test
  public void testLoadContextTokenRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("molgenis-token", "my_token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    HttpRequestResponseHolder holder = new HttpRequestResponseHolder(request, response);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(tokenSecurityContextRepository.loadContext(holder)).thenReturn(securityContext);
    assertEquals(tokenAwareSecurityContextRepository.loadContext(holder), securityContext);
  }

  @Test
  public void testSaveContext() {
    SecurityContext securityContext = mock(SecurityContext.class);
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    tokenAwareSecurityContextRepository.saveContext(securityContext, request, response);
    verify(defaultSecurityContextRepository).saveContext(securityContext, request, response);
  }

  @Test
  public void testSaveContextTokenRequest() {
    SecurityContext securityContext = mock(SecurityContext.class);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("molgenis-token", "my_token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    tokenAwareSecurityContextRepository.saveContext(securityContext, request, response);
    verify(tokenSecurityContextRepository).saveContext(securityContext, request, response);
  }

  @Test
  public void testContainsContext() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    when(defaultSecurityContextRepository.containsContext(request)).thenReturn(true);
    assertTrue(tokenAwareSecurityContextRepository.containsContext(request));
  }

  @Test
  public void testContainsContextTokenRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("molgenis-token", "my_token");
    when(tokenSecurityContextRepository.containsContext(request)).thenReturn(true);
    assertTrue(tokenAwareSecurityContextRepository.containsContext(request));
  }
}
