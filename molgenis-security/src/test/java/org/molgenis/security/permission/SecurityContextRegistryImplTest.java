package org.molgenis.security.permission;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;

class SecurityContextRegistryImplTest {
  private SecurityContextRegistryImpl securityContextRegistry;

  private String httpSessionWithSecurityContextId;
  private HttpSession httpSessionWithSecurityContext;
  private SecurityContext securityContext;
  private String httpSessionWithoutSecurityContextId;

  @BeforeEach
  void setUpBeforeMethod() {
    securityContextRegistry = new SecurityContextRegistryImpl();

    httpSessionWithSecurityContextId = "sessionWithSecurityContext";
    httpSessionWithSecurityContext =
        when(mock(HttpSession.class).getId())
            .thenReturn(httpSessionWithSecurityContextId)
            .getMock();
    securityContext = mock(SecurityContext.class);
    when(httpSessionWithSecurityContext.getAttribute("SPRING_SECURITY_CONTEXT"))
        .thenReturn(securityContext);
    securityContextRegistry.handleHttpSessionCreatedEvent(
        new HttpSessionCreatedEvent(httpSessionWithSecurityContext));

    httpSessionWithoutSecurityContextId = "sessionWithoutSecurityContext";
    HttpSession httpSessionWithoutSecurityContext =
        when(mock(HttpSession.class).getId())
            .thenReturn(httpSessionWithoutSecurityContextId)
            .getMock();
    securityContextRegistry.handleHttpSessionCreatedEvent(
        new HttpSessionCreatedEvent(httpSessionWithoutSecurityContext));
  }

  @Test
  void testGetSecurityContextFromSessionWithSecurityContext() {
    assertEquals(
        securityContext,
        securityContextRegistry.getSecurityContext(httpSessionWithSecurityContextId));
  }

  @Test
  void testGetSecurityContextFromSessionWithoutSecurityContext() {
    assertNull(securityContextRegistry.getSecurityContext(httpSessionWithoutSecurityContextId));
  }

  @Test
  void testGetSecurityContextFromSessionUnexpectedValue() {
    String corruptHttpSessionId = "corruptSessionId";
    HttpSession corruptHttpSession =
        when(mock(HttpSession.class).getId()).thenReturn(corruptHttpSessionId).getMock();
    when(corruptHttpSession.getAttribute("SPRING_SECURITY_CONTEXT"))
        .thenReturn("corruptSecurityContext");
    securityContextRegistry.handleHttpSessionCreatedEvent(
        new HttpSessionCreatedEvent(corruptHttpSession));
    Exception exception =
        assertThrows(
            RuntimeException.class,
            () -> securityContextRegistry.getSecurityContext(corruptHttpSessionId));
    assertThat(exception.getMessage())
        .containsPattern(
            "Session attribute 'SPRING_SECURITY_CONTEXT' is of type 'String' instead of 'SecurityContext'");
  }

  @Test
  void testGetSecurityContextInvalidatedSession() {
    String corruptHttpSessionId = "invalidSessionId";
    HttpSession corruptHttpSession =
        when(mock(HttpSession.class).getId()).thenReturn(corruptHttpSessionId).getMock();
    doThrow(IllegalStateException.class)
        .when(corruptHttpSession)
        .getAttribute("SPRING_SECURITY_CONTEXT");
    securityContextRegistry.handleHttpSessionCreatedEvent(
        new HttpSessionCreatedEvent(corruptHttpSession));
    assertNull(securityContextRegistry.getSecurityContext(corruptHttpSessionId));
  }

  @Test
  void testGetSecurityContextUnknownSessionId() {
    assertNull(securityContextRegistry.getSecurityContext("unknownSessionId"));
  }

  @Test
  void testGetSecurityContexts() {
    assertEquals(
        singletonList(securityContext),
        securityContextRegistry.getSecurityContexts().collect(toList()));
  }

  @Test
  void testHandleHttpSessionDestroyedEvent() {
    securityContextRegistry.handleHttpSessionDestroyedEvent(
        new HttpSessionDestroyedEvent(httpSessionWithSecurityContext));
  }
}
