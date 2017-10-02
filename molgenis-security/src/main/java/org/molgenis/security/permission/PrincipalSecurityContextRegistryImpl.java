package org.molgenis.security.permission;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Component
public class PrincipalSecurityContextRegistryImpl implements PrincipalSecurityContextRegistry
{
	private final SecurityContextRegistry securityContextRegistry;

	PrincipalSecurityContextRegistryImpl(SecurityContextRegistry securityContextRegistry)
	{
		this.securityContextRegistry = requireNonNull(securityContextRegistry);
	}

	@Override
	public Stream<SecurityContext> getSecurityContexts(Object principal)
	{
		Set<SecurityContext> securityContexts = new HashSet<>();

		SecurityContext currentExecutionThreadSecurityContext = getSecurityContextCurrentExecutionThread(principal);
		if (currentExecutionThreadSecurityContext != null)
		{
			securityContexts.add(currentExecutionThreadSecurityContext);
		}

		getSecurityContextsFromRegistry(principal).forEach(securityContexts::add);

		return securityContexts.stream();
	}

	private SecurityContext getSecurityContextCurrentExecutionThread(Object principal)
	{
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		if (authentication != null && authentication.getPrincipal().equals(principal))
		{
			return securityContext;
		}
		else
		{
			return null;
		}
	}

	private Stream<SecurityContext> getSecurityContextsFromRegistry(Object principal)
	{
		String username = getUsername(principal);
		return securityContextRegistry.getSecurityContexts().filter(securityContext ->
		{
			Authentication authentication = securityContext.getAuthentication();
			if (authentication != null)
			{
				Object securityContextPrincipal = authentication.getPrincipal();
				if (username.equals(getUsername(securityContextPrincipal)))
				{
					return true;
				}
			}
			return false;
		});
	}

	private String getUsername(Object principal)
	{
		String username;
		if (principal instanceof User)
		{
			username = ((User) principal).getUsername();
		}
		else
		{
			username = principal.toString();
		}
		return username;
	}
}
