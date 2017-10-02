package org.molgenis.security.permission;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpSession;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNull;

public class SecurityContextRegistryImplTest
{
	private SecurityContextRegistryImpl securityContextRegistry;

	private String httpSessionWithSecurityContextId;
	private HttpSession httpSessionWithSecurityContext;
	private SecurityContext securityContext;
	private String httpSessionWithoutSecurityContextId;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		securityContextRegistry = new SecurityContextRegistryImpl();

		httpSessionWithSecurityContextId = "sessionWithSecurityContext";
		httpSessionWithSecurityContext = when(mock(HttpSession.class).getId()).thenReturn(
				httpSessionWithSecurityContextId).getMock();
		securityContext = mock(SecurityContext.class);
		when(httpSessionWithSecurityContext.getAttribute("SPRING_SECURITY_CONTEXT")).thenReturn(securityContext);
		securityContextRegistry.handleHttpSessionCreatedEvent(
				new HttpSessionCreatedEvent(httpSessionWithSecurityContext));

		httpSessionWithoutSecurityContextId = "sessionWithoutSecurityContext";
		HttpSession httpSessionWithoutSecurityContext = when(mock(HttpSession.class).getId()).thenReturn(
				httpSessionWithoutSecurityContextId).getMock();
		securityContextRegistry.handleHttpSessionCreatedEvent(
				new HttpSessionCreatedEvent(httpSessionWithoutSecurityContext));
	}

	@Test
	public void testGetSecurityContextFromSessionWithSecurityContext()
	{
		assertEquals(securityContextRegistry.getSecurityContext(httpSessionWithSecurityContextId), securityContext);
	}

	@Test
	public void testGetSecurityContextFromSessionWithoutSecurityContext()
	{
		assertNull(securityContextRegistry.getSecurityContext(httpSessionWithoutSecurityContextId));
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Session attribute 'SPRING_SECURITY_CONTEXT' is of type 'String' instead of 'SecurityContext'")
	public void testGetSecurityContextFromSessionUnexpectedValue()
	{
		String corruptHttpSessionId = "corruptSessionId";
		HttpSession corruptHttpSession = when(mock(HttpSession.class).getId()).thenReturn(corruptHttpSessionId)
																			  .getMock();
		when(corruptHttpSession.getAttribute("SPRING_SECURITY_CONTEXT")).thenReturn("corruptSecurityContext");
		securityContextRegistry.handleHttpSessionCreatedEvent(new HttpSessionCreatedEvent(corruptHttpSession));
		assertNull(securityContextRegistry.getSecurityContext(corruptHttpSessionId));
	}

	@Test
	public void testGetSecurityContextInvalidatedSession()
	{
		String corruptHttpSessionId = "invalidSessionId";
		HttpSession corruptHttpSession = when(mock(HttpSession.class).getId()).thenReturn(corruptHttpSessionId)
																			  .getMock();
		doThrow(IllegalStateException.class).when(corruptHttpSession).getAttribute("SPRING_SECURITY_CONTEXT");
		securityContextRegistry.handleHttpSessionCreatedEvent(new HttpSessionCreatedEvent(corruptHttpSession));
		assertNull(securityContextRegistry.getSecurityContext(corruptHttpSessionId));
	}

	@Test
	public void testGetSecurityContextUnknownSessionId()
	{
		assertNull(securityContextRegistry.getSecurityContext("unknownSessionId"));
	}

	@Test
	public void testGetSecurityContexts()
	{
		assertEquals(securityContextRegistry.getSecurityContexts().collect(Collectors.toList()),
				singletonList(securityContext));
	}

	@Test
	public void testHandleHttpSessionDestroyedEvent()
	{
		securityContextRegistry.handleHttpSessionDestroyedEvent(
				new HttpSessionDestroyedEvent(httpSessionWithSecurityContext));
	}
}