package org.molgenis.web.i18n;

import org.mockito.Answers;
import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.User;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.function.Supplier;

import static java.util.Locale.GERMAN;
import static org.mockito.Mockito.*;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.testng.Assert.assertEquals;

public class MolgenisLocaleResolverTest extends AbstractMolgenisSpringTest
{
	private MolgenisLocaleResolver molgenisLocaleResolver;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private DataService dataService;
	@Mock
	private Supplier<Locale> defaultLocaleSupplier;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private User user;

	@BeforeMethod
	public void setUp()
	{
		molgenisLocaleResolver = new MolgenisLocaleResolver(dataService, defaultLocaleSupplier);
	}

	@Test
	public void testResolveLocaleNotAuthenticated()
	{
		when(defaultLocaleSupplier.get()).thenReturn(GERMAN);
		assertEquals(molgenisLocaleResolver.resolveLocale(request), GERMAN);
		verifyZeroInteractions(dataService);
	}

	@Test
	@WithAnonymousUser
	public void testResolveLocaleAnonymous()
	{
		when(defaultLocaleSupplier.get()).thenReturn(GERMAN);
		assertEquals(molgenisLocaleResolver.resolveLocale(request), GERMAN);
		verifyZeroInteractions(dataService);
	}

	@Test
	@WithMockUser
	public void testResolveLocaleAuthenticated()
	{
		when(dataService.query(USER, User.class).eq("username", "user").findOne()).thenReturn(user);
		when(user.getLanguageCode()).thenReturn("de");
		assertEquals(molgenisLocaleResolver.resolveLocale(request), GERMAN);
		verifyZeroInteractions(defaultLocaleSupplier);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	@WithMockUser
	public void testSetLocaleUnusedLanguage()
	{
		molgenisLocaleResolver.setLocale(request, response, Locale.KOREAN);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testSetLocaleNotAuthenticated()
	{
		molgenisLocaleResolver.setLocale(request, response, GERMAN);
	}

	@Test
	@WithMockUser
	public void testSetLocale()
	{
		when(dataService.query(USER, User.class).eq("username", "user").findOne()).thenReturn(user);
		molgenisLocaleResolver.setLocale(request, response, GERMAN);
		verify(user).setLanguageCode("de");
		verify(dataService).update(USER, user);
	}
}