package org.molgenis.security.permission;

import org.springframework.security.core.context.SecurityContext;

import java.util.stream.Stream;

public interface PrincipalSecurityContextRegistry
{
	Stream<SecurityContext> getSecurityContexts(Object principal);
}
