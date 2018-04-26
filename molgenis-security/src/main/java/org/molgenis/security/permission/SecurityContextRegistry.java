package org.molgenis.security.permission;

import org.springframework.security.core.context.SecurityContext;

import java.util.stream.Stream;

public interface SecurityContextRegistry
{
	/**
	 * Returns the security context for the session with the given id.
	 *
	 * @param sessionId session identifier
	 * @return security context or null if the session does not exist or doesn't contain a security context
	 */
	SecurityContext getSecurityContext(String sessionId);

	/**
	 * Returns a stream of all security contexts
	 *
	 * @return stream of all security contexts
	 */
	Stream<SecurityContext> getSecurityContexts();
}
