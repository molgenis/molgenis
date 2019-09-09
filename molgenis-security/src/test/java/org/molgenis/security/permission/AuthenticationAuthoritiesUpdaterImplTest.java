package org.molgenis.security.permission;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.security.token.RestAuthenticationToken;
import org.molgenis.security.twofactor.auth.RecoveryAuthenticationToken;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationToken;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

class AuthenticationAuthoritiesUpdaterImplTest {
  private AuthenticationAuthoritiesUpdaterImpl authenticationAuthoritiesUpdaterImpl;
  private List<GrantedAuthority> updatedAuthorities;

  @BeforeEach
  void setUpBeforeMethod() {
    authenticationAuthoritiesUpdaterImpl = new AuthenticationAuthoritiesUpdaterImpl();
    updatedAuthorities = singletonList(new SimpleGrantedAuthority("role"));
  }

  @Test
  void testUpdateAuthenticationTwoFactorAuthenticationToken() {
    Object principal = mock(Object.class);
    Object credentials = mock(Object.class);
    String verificationCode = "dummyVerificationCode";
    String secretKey = "secretKey";
    TwoFactorAuthenticationToken twoFactorAuthenticationToken =
        new TwoFactorAuthenticationToken(
            principal, credentials, Collections.emptyList(), verificationCode, secretKey);

    Authentication updatedAuthentication =
        authenticationAuthoritiesUpdaterImpl.updateAuthentication(
            twoFactorAuthenticationToken, updatedAuthorities);
    assertEquals(
        updatedAuthentication,
        new TwoFactorAuthenticationToken(
            principal, credentials, updatedAuthorities, verificationCode, secretKey));
  }

  @Test
  void testUpdateAuthenticationSystemSecurityToken() {
    SystemSecurityToken systemSecurityToken = mock(SystemSecurityToken.class);
    Authentication updatedAuthentication =
        authenticationAuthoritiesUpdaterImpl.updateAuthentication(
            systemSecurityToken, updatedAuthorities);
    assertEquals(updatedAuthentication, systemSecurityToken);
  }

  @Test
  void testUpdateAuthenticationRestAuthenticationToken() {
    Object principal = mock(Object.class);
    Object credentials = mock(Object.class);
    String token = "token";
    RestAuthenticationToken restAuthenticationToken =
        new RestAuthenticationToken(principal, credentials, emptyList(), token);

    Authentication updatedAuthentication =
        authenticationAuthoritiesUpdaterImpl.updateAuthentication(
            restAuthenticationToken, updatedAuthorities);
    assertEquals(
        updatedAuthentication,
        new RestAuthenticationToken(principal, credentials, updatedAuthorities, token));
  }

  @Test
  void testUpdateAuthenticationRecoveryAuthenticationToken() {
    Object principal = mock(Object.class);
    Object credentials = mock(Object.class);
    String recoveryCode = "recoveryCode";
    RecoveryAuthenticationToken recoveryAuthenticationToken =
        new RecoveryAuthenticationToken(principal, credentials, emptyList(), recoveryCode);

    Authentication updatedAuthentication =
        authenticationAuthoritiesUpdaterImpl.updateAuthentication(
            recoveryAuthenticationToken, updatedAuthorities);
    assertEquals(
        updatedAuthentication,
        new RecoveryAuthenticationToken(principal, credentials, updatedAuthorities, recoveryCode));
  }

  @Test
  void testUpdateAuthenticationUsernamePasswordAuthenticationToken() {
    Object principal = mock(Object.class);
    Object credentials = mock(Object.class);
    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
        new UsernamePasswordAuthenticationToken(principal, credentials, emptyList());

    Authentication updatedAuthentication =
        authenticationAuthoritiesUpdaterImpl.updateAuthentication(
            usernamePasswordAuthenticationToken, updatedAuthorities);
    assertEquals(
        updatedAuthentication,
        new UsernamePasswordAuthenticationToken(principal, credentials, updatedAuthorities));
  }

  @Test
  void testUpdateAuthenticationRunAsUserToken() {
    String key = "key";
    Object principal = mock(Object.class);
    Object credentials = mock(Object.class);
    Class<? extends Authentication> originalAuthentication = Authentication.class;
    RunAsUserToken runAsUserToken =
        new RunAsUserToken(key, principal, credentials, emptyList(), originalAuthentication);

    Authentication updatedAuthentication =
        authenticationAuthoritiesUpdaterImpl.updateAuthentication(
            runAsUserToken, updatedAuthorities);
    assertEquals(
        updatedAuthentication,
        new RunAsUserToken(
            key, principal, credentials, updatedAuthorities, originalAuthentication));
  }

  @Test
  void testUpdateAuthenticationAnonymousAuthenticationToken() {
    String key = "key";
    Object principal = mock(Object.class);
    AnonymousAuthenticationToken anonymousAuthenticationToken =
        new AnonymousAuthenticationToken(
            key, principal, singletonList(mock(GrantedAuthority.class)));

    Authentication updatedAuthentication =
        authenticationAuthoritiesUpdaterImpl.updateAuthentication(
            anonymousAuthenticationToken, updatedAuthorities);
    assertEquals(
        updatedAuthentication,
        new AnonymousAuthenticationToken(key, principal, updatedAuthorities));
  }

  @Test
  void testUpdateAuthenticationUnknownToken() {
    Exception exception =
        assertThrows(
            SessionAuthenticationException.class,
            () ->
                authenticationAuthoritiesUpdaterImpl.updateAuthentication(
                    mock(Authentication.class), updatedAuthorities));
    assertThat(exception.getMessage()).containsPattern("Unknown authentication type '.*?'");
  }
}
