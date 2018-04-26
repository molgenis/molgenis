package org.molgenis.security.permission;

import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Component
public class SecurityContextRegistryImpl implements SecurityContextRegistry
{
	private final ConcurrentMap<String, HttpSession> httpSessionMap;

	SecurityContextRegistryImpl()
	{
		httpSessionMap = new ConcurrentHashMap<>();
	}

	@Override
	public SecurityContext getSecurityContext(String sessionId)
	{
		HttpSession httpSession = httpSessionMap.get(sessionId);
		if (httpSession == null)
		{
			return null;
		}
		return getSecurityContext(httpSession);
	}

	@Override
	public Stream<SecurityContext> getSecurityContexts()
	{
		return httpSessionMap.values().stream().map(this::getSecurityContext).filter(Objects::nonNull);
	}

	private SecurityContext getSecurityContext(HttpSession httpSession)
	{
		Object securityContext;
		try
		{
			securityContext = httpSession.getAttribute(SPRING_SECURITY_CONTEXT_KEY);
		}
		catch (IllegalStateException e)
		{
			// session was invalidated
			return null;
		}
		if (securityContext == null)
		{
			return null;
		}
		if (!(securityContext instanceof SecurityContext))
		{
			throw new IllegalStateException(
					String.format("Session attribute '%s' is of type '%s' instead of '%s'", SPRING_SECURITY_CONTEXT_KEY,
							securityContext.getClass().getSimpleName(), SecurityContext.class.getSimpleName()));
		}
		return (SecurityContext) securityContext;
	}

	@EventListener
	public void handleHttpSessionCreatedEvent(HttpSessionCreatedEvent httpSessionCreatedEvent)
	{
		HttpSession session = httpSessionCreatedEvent.getSession();
		httpSessionMap.put(session.getId(), session);
	}

	@EventListener
	public void handleHttpSessionDestroyedEvent(HttpSessionDestroyedEvent httpSessionDestroyedEvent)
	{
		String sessionId = httpSessionDestroyedEvent.getId();
		httpSessionMap.remove(sessionId);
	}
}
