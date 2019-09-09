package org.molgenis.security.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.core.token.UnknownTokenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsChecker;

class TokenAuthenticationProviderTest {
  private TokenAuthenticationProvider tokenAuthenticationProvider;
  private TokenService tokenService;

  @BeforeEach
  void beforeMethod() {
    tokenService = mock(TokenService.class);
    tokenAuthenticationProvider =
        new TokenAuthenticationProvider(tokenService, mock(UserDetailsChecker.class));
  }

  @Test
  void authenticate() {
    RestAuthenticationToken authToken = new RestAuthenticationToken("token");
    assertFalse(authToken.isAuthenticated());

    when(tokenService.findUserByToken("token"))
        .thenReturn(
            new User("username", "password", Arrays.asList(new SimpleGrantedAuthority("admin"))));

    Authentication auth = tokenAuthenticationProvider.authenticate(authToken);
    assertNotNull(auth);
    assertTrue(auth.isAuthenticated());
    assertEquals(auth.getName(), "username");
    assertEquals(auth.getAuthorities().size(), 1);
    assertEquals(auth.getAuthorities().iterator().next().getAuthority(), "admin");
  }

  @Test
  void authenticateInvalidToken() {
    when(tokenService.findUserByToken("token"))
        .thenThrow(new UnknownTokenException("Invalid token"));
    assertThrows(
        AuthenticationException.class,
        () -> tokenAuthenticationProvider.authenticate(new RestAuthenticationToken("token")));
  }
}
