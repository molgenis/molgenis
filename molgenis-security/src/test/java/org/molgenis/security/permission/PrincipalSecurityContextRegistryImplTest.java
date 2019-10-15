package org.molgenis.security.permission;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;

@SecurityTestExecutionListeners
class PrincipalSecurityContextRegistryImplTest extends AbstractMockitoSpringContextTests {
  @Mock private SecurityContextRegistry securityContextRegistry;

  private PrincipalSecurityContextRegistryImpl principalSecurityContextRegistryImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    principalSecurityContextRegistryImpl =
        new PrincipalSecurityContextRegistryImpl(securityContextRegistry);
  }

  @WithMockUser(username = "user")
  @Test
  void testGetSecurityContextsUserThreadNoUserSessions() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Object user = securityContext.getAuthentication().getPrincipal();
    assertEquals(
        singletonList(securityContext),
        principalSecurityContextRegistryImpl.getSecurityContexts(user).collect(toList()));
  }

  @WithMockUser(username = "systemUser")
  @Test
  void testGetSecurityContextsNoUserThreadNoUserSessions() {
    Object user = when(mock(User.class).getUsername()).thenReturn("user").getMock();
    assertEquals(
        emptyList(),
        principalSecurityContextRegistryImpl.getSecurityContexts(user).collect(toList()));
  }

  @WithMockUser(username = "user")
  @Test
  void testGetSecurityContextsUserThreadSessions() {
    Authentication userAuthentication = SecurityContextHolder.getContext().getAuthentication();
    SecurityContext securityContextUser0 = mock(SecurityContext.class);
    when(securityContextUser0.getAuthentication()).thenReturn(userAuthentication);
    SecurityContext securityContextUser1 = mock(SecurityContext.class);
    when(securityContextUser1.getAuthentication()).thenReturn(userAuthentication);
    SecurityContext securityContextOtherUser = mock(SecurityContext.class);
    Authentication otherUserAuthentication =
        when(mock(Authentication.class).getPrincipal()).thenReturn("otherUser").getMock();
    when(securityContextOtherUser.getAuthentication()).thenReturn(otherUserAuthentication);

    when(securityContextRegistry.getSecurityContexts())
        .thenReturn(
            Stream.of(securityContextUser0, securityContextUser1, securityContextOtherUser));
    Object user = when(mock(User.class).getUsername()).thenReturn("user").getMock();
    assertEquals(
        new HashSet<>(asList(securityContextUser0, securityContextUser1)),
        principalSecurityContextRegistryImpl.getSecurityContexts(user).collect(toSet()));
  }
}
