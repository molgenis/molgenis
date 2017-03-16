package org.molgenis.settings.mail;

import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.util.mail.MailSenderFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MailSettingsRepositoryDecoratorTest
{
	private MailSettingsRepositoryDecorator mailSettingsRepositoryDecorator;
	@Mock
	private MailSettingsImpl mailSettings;
	@Mock
	private MailSenderFactory mailSenderFactory;
	@Mock
	private Repository<MailSettingsImpl> decorated;

	@BeforeMethod
	public void beforeMethod()
	{
		mailSettingsRepositoryDecorator = new MailSettingsRepositoryDecorator(decorated, mailSenderFactory);
	}

	@DataProvider(name = "addDontTest")
	public Object[][] addDontTest()
	{
		return new Object[][] { { false, "username", "password" }, { true, null, "password" },
				{ true, "username", null } };
	}

	@Test(dataProvider = "addDontTest")
	public void testAddDontTestConnection(boolean testConnection, String username, String password)
	{
		when(mailSettings.isTestConnection()).thenReturn(testConnection);
		when(mailSettings.getUsername()).thenReturn(username);
		when(mailSettings.getPassword()).thenReturn(password);

		mailSettingsRepositoryDecorator.add(mailSettings);
		verify(mailSenderFactory, never()).validateConnection(any(MailSettingsImpl.class));
		verify(decorated).add(mailSettings);
		verifyNoMoreInteractions(decorated);
	}

	@Test
	public void testAddValidSettings()
	{
		when(mailSettings.isTestConnection()).thenReturn(true);
		when(mailSettings.getUsername()).thenReturn("Username");
		when(mailSettings.getPassword()).thenReturn("password");

		mailSettingsRepositoryDecorator.add(mailSettings);
		verify(mailSenderFactory).validateConnection(any(MailSettingsImpl.class));
		verify(decorated).add(mailSettings);
		verifyNoMoreInteractions(decorated);
	}

	@Test
	public void testAddInvalidSettings()
	{
		when(mailSettings.isTestConnection()).thenReturn(true);
		when(mailSettings.getUsername()).thenReturn("Username");
		when(mailSettings.getPassword()).thenReturn("password");

		doThrow(IllegalStateException.class).when(mailSenderFactory).validateConnection(any(MailSettingsImpl.class));

		try
		{
			mailSettingsRepositoryDecorator.add(mailSettings);
			Assert.fail("Should've thrown exception.");
		}
		catch (IllegalStateException expected)
		{
			verifyZeroInteractions(decorated);
		}
	}

	@Test(dataProvider = "addDontTest")
	public void testUpdateDontTestConnection(boolean testConnection, String username, String password)
	{
		when(mailSettings.isTestConnection()).thenReturn(testConnection);
		when(mailSettings.getUsername()).thenReturn(username);
		when(mailSettings.getPassword()).thenReturn(password);
		mailSettingsRepositoryDecorator.update(mailSettings);
		verify(mailSenderFactory, never()).validateConnection(any(MailSettingsImpl.class));
		verify(decorated).update(mailSettings);
		verifyNoMoreInteractions(decorated);
	}

	@Test
	public void testUpdateValidSettings()
	{
		when(mailSettings.isTestConnection()).thenReturn(true);
		when(mailSettings.getUsername()).thenReturn("Username");
		when(mailSettings.getPassword()).thenReturn("password");

		mailSettingsRepositoryDecorator.update(mailSettings);
		verify(mailSenderFactory).validateConnection(any(MailSettingsImpl.class));
		verify(decorated).update(mailSettings);
		verifyNoMoreInteractions(decorated);
	}

	@Test
	public void testUpdateInvalidSettings()
	{
		when(mailSettings.isTestConnection()).thenReturn(true);
		when(mailSettings.getUsername()).thenReturn("Username");
		when(mailSettings.getPassword()).thenReturn("password");

		doThrow(IllegalStateException.class).when(mailSenderFactory).validateConnection(any(MailSettingsImpl.class));

		try
		{
			mailSettingsRepositoryDecorator.update(mailSettings);
			Assert.fail("Should've thrown exception.");
		}
		catch (IllegalStateException expected)
		{
			verifyZeroInteractions(decorated);
		}
	}
}
