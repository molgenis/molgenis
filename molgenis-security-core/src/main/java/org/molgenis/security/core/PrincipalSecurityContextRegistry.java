package org.molgenis.security.core;

import java.util.stream.Stream;
import org.springframework.security.core.context.SecurityContext;

public interface PrincipalSecurityContextRegistry {
  Stream<SecurityContext> getSecurityContexts(Object principal);
}
