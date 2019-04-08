package org.molgenis.security;

import static java.util.Objects.requireNonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.molgenis.security.token.TokenExtractor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Security context repository that uses a different strategy for loading/saving security contexts
 * for requests containing a molgenis authentication token.
 */
class TokenAwareSecurityContextRepository implements SecurityContextRepository {
  private final SecurityContextRepository tokenSecurityContextRepository;
  private final SecurityContextRepository defaultSecurityContextRepository;

  TokenAwareSecurityContextRepository(
      SecurityContextRepository tokenSecurityContextRepository,
      SecurityContextRepository defaultSecurityContextRepository) {
    this.tokenSecurityContextRepository = requireNonNull(tokenSecurityContextRepository);
    this.defaultSecurityContextRepository = requireNonNull(defaultSecurityContextRepository);
  }

  @Override
  public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
    HttpServletRequest request = requestResponseHolder.getRequest();
    return getSecurityContextRepository(request).loadContext(requestResponseHolder);
  }

  @Override
  public void saveContext(
      SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
    getSecurityContextRepository(request).saveContext(context, request, response);
  }

  @Override
  public boolean containsContext(HttpServletRequest request) {
    return getSecurityContextRepository(request).containsContext(request);
  }

  private SecurityContextRepository getSecurityContextRepository(HttpServletRequest request) {
    boolean hasToken = TokenExtractor.getToken(request) != null;
    return hasToken ? tokenSecurityContextRepository : defaultSecurityContextRepository;
  }
}
