package org.molgenis.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

class TokenAwareSecurityContextRepositoryTest extends AbstractMockitoTest {
  @Mock private SecurityContextRepository tokenSecurityContextRepository;
  @Mock private SecurityContextRepository defaultSecurityContextRepository;
  private TokenAwareSecurityContextRepository tokenAwareSecurityContextRepository;

  @BeforeEach
  void setUpBeforeMethod() {
    tokenAwareSecurityContextRepository =
        new TokenAwareSecurityContextRepository(
            tokenSecurityContextRepository, defaultSecurityContextRepository);
  }

  @Test
  void testTokenAwareSecurityContextRepository() {
    assertThrows(
        NullPointerException.class, () -> new TokenAwareSecurityContextRepository(null, null));
  }

  @Test
  void testLoadContext() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    HttpRequestResponseHolder holder = new HttpRequestResponseHolder(request, response);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(defaultSecurityContextRepository.loadContext(holder)).thenReturn(securityContext);
    assertEquals(securityContext, tokenAwareSecurityContextRepository.loadContext(holder));
  }

  @Test
  void testLoadContextTokenRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("molgenis-token", "my_token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    HttpRequestResponseHolder holder = new HttpRequestResponseHolder(request, response);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(tokenSecurityContextRepository.loadContext(holder)).thenReturn(securityContext);
    assertEquals(securityContext, tokenAwareSecurityContextRepository.loadContext(holder));
  }

  @Test
  void testSaveContext() {
    SecurityContext securityContext = mock(SecurityContext.class);
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    tokenAwareSecurityContextRepository.saveContext(securityContext, request, response);
    verify(defaultSecurityContextRepository).saveContext(securityContext, request, response);
  }

  @Test
  void testSaveContextTokenRequest() {
    SecurityContext securityContext = mock(SecurityContext.class);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("molgenis-token", "my_token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    tokenAwareSecurityContextRepository.saveContext(securityContext, request, response);
    verify(tokenSecurityContextRepository).saveContext(securityContext, request, response);
  }

  @Test
  void testContainsContext() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    when(defaultSecurityContextRepository.containsContext(request)).thenReturn(true);
    assertTrue(tokenAwareSecurityContextRepository.containsContext(request));
  }

  @Test
  void testContainsContextTokenRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("molgenis-token", "my_token");
    when(tokenSecurityContextRepository.containsContext(request)).thenReturn(true);
    assertTrue(tokenAwareSecurityContextRepository.containsContext(request));
  }
}
