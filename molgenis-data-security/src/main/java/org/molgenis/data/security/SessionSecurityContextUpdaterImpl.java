package org.molgenis.data.security;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.transaction.TransactionListener;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.security.core.AuthenticationAuthoritiesUpdater;
import org.molgenis.security.core.PrincipalSecurityContextRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class SessionSecurityContextUpdaterImpl
    implements SessionSecurityContextUpdater, TransactionListener {

  private final PrincipalSecurityContextRegistry principalSecurityContextRegistry;
  private final AuthenticationAuthoritiesUpdater authenticationAuthoritiesUpdater;
  private final UserDetailsService userDetailsService;

  private final ThreadLocal<List<String>> actionThreadLocal;

  SessionSecurityContextUpdaterImpl(
      PrincipalSecurityContextRegistry principalSecurityContextRegistry,
      AuthenticationAuthoritiesUpdater authenticationAuthoritiesUpdater,
      UserDetailsService userDetailsService,
      TransactionManager transactionManager) {
    this.principalSecurityContextRegistry = requireNonNull(principalSecurityContextRegistry);
    this.authenticationAuthoritiesUpdater = requireNonNull(authenticationAuthoritiesUpdater);
    this.userDetailsService = requireNonNull(userDetailsService);

    transactionManager.addTransactionListener(this);
    actionThreadLocal = new ThreadLocal<>();
  }

  @Override
  public void resetAuthorities(User user) {
    actionThreadLocal.get().add(user.getUsername());
  }

  @Override
  public void transactionStarted(String transactionId) {
    actionThreadLocal.set(new ArrayList<>());
  }

  @Override
  public void afterCommitTransaction(String transactionId) {
    List<String> usernames = actionThreadLocal.get();
    if (usernames != null && !usernames.isEmpty()) {
      usernames.forEach(this::resetAuthorities);
      usernames.clear();
    }
  }

  @Override
  public void doCleanupAfterCompletion(String transactionId) {
    actionThreadLocal.remove();
  }

  private synchronized void resetAuthorities(String username) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    Collection<? extends GrantedAuthority> updatedAuthorities = userDetails.getAuthorities();

    Object principal = toPrincipal(username);
    principalSecurityContextRegistry
        .getSecurityContexts(principal)
        .forEach(securityContext -> resetAuthorities(securityContext, updatedAuthorities));
  }

  private void resetAuthorities(
      SecurityContext securityContext, Collection<? extends GrantedAuthority> authorities) {
    Authentication updatedAuthentication =
        authenticationAuthoritiesUpdater.updateAuthentication(
            securityContext.getAuthentication(), new ArrayList<>(authorities));
    securityContext.setAuthentication(updatedAuthentication);
  }

  private Object toPrincipal(String username) {
    return username;
  }
}
