package org.molgenis.security.token;

import static java.util.Objects.requireNonNull;

import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.token.TokenService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

/** AuthenticationProvider that uses the TokenService and expects a RestAuthenticationToken */
public class TokenAuthenticationProvider implements AuthenticationProvider {
  private final TokenService tokenService;
  private UserDetailsChecker userDetailsChecker;

  public TokenAuthenticationProvider(
      TokenService tokenService, UserDetailsChecker userDetailsChecker) {
    this.tokenService = requireNonNull(tokenService);
    this.userDetailsChecker = requireNonNull(userDetailsChecker);
  }

  @Override
  @RunAsSystem
  public Authentication authenticate(Authentication authentication) {
    if (!supports(authentication.getClass()))
      throw new IllegalArgumentException("Only RestAuthenticationToken is supported");

    RestAuthenticationToken authToken = (RestAuthenticationToken) authentication;

    if (authToken.getToken() != null) {
      UserDetails userDetails =
          tokenService.findUserByToken(authToken.getToken()); // Throws UnknownTokenException
      userDetailsChecker.check(userDetails);
      // if token is invalid
      authToken =
          new RestAuthenticationToken(
              userDetails,
              userDetails.getPassword(),
              userDetails.getAuthorities(),
              authToken.getToken());
    }

    return authToken;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return RestAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
