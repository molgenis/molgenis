package org.molgenis.data.security;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.security.core.AuthenticationAuthoritiesUpdater;
import org.molgenis.security.core.PrincipalSecurityContextRegistry;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

class SessionSecurityContextUpdaterImplTest extends AbstractMockitoTest {
  @Mock private PrincipalSecurityContextRegistry principalSecurityContextRegistry;
  @Mock private AuthenticationAuthoritiesUpdater authenticationAuthoritiesUpdater;
  @Mock private TransactionManager transactionManager;
  @Mock private UserDetailsService userDetailsService;
  private SessionSecurityContextUpdaterImpl sessionSecurityContextUpdaterImpl;

  @BeforeEach
  void SessionSecurityContextUpdaterImpl() {
    sessionSecurityContextUpdaterImpl =
        new SessionSecurityContextUpdaterImpl(
            principalSecurityContextRegistry,
            authenticationAuthoritiesUpdater,
            userDetailsService,
            transactionManager);
  }

  @SuppressWarnings("unchecked")
  @Test
  void resetAuthority() {
    String username = "MyUsername";
    User user = when(mock(User.class).getUsername()).thenReturn(username).getMock();
    String transactionId = "MyTransactionId";

    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    when(principalSecurityContextRegistry.getSecurityContexts(username))
        .thenReturn(Stream.of(securityContext));
    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getAuthorities())
        .thenReturn((Collection) singletonList(new SimpleGrantedAuthority("ROLE_MyRoleName")));
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    sessionSecurityContextUpdaterImpl.transactionStarted(transactionId);
    try {
      sessionSecurityContextUpdaterImpl.resetAuthorities(user);
      sessionSecurityContextUpdaterImpl.afterCommitTransaction(transactionId);
    } finally {
      sessionSecurityContextUpdaterImpl.doCleanupAfterCompletion(transactionId);
    }
    verify(authenticationAuthoritiesUpdater)
        .updateAuthentication(
            authentication, singletonList(new SimpleGrantedAuthority("ROLE_MyRoleName")));
  }
}
