package org.molgenis.settings.mail;

import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.mail.MailSenderFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MailSettingsRepositoryDecoratorTest extends AbstractMockitoTest
{
	private MailSettingsRepositoryDecorator mailSettingsRepositoryDecorator;
	@Mock
	private Entity entity;
	@Mock
	private MailSenderFactory mailSenderFactory;
	@Mock
	private Repository<Entity> decorated;

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
		when(entity.getBoolean(MailSettingsImpl.Meta.TEST_CONNECTION)).thenReturn(testConnection);
		when(entity.getString(MailSettingsImpl.Meta.USERNAME)).thenReturn(username);
		when(entity.getString(MailSettingsImpl.Meta.PASSWORD)).thenReturn(password);
		mailSettingsRepositoryDecorator.add(entity);
		verify(mailSenderFactory, never()).validateConnection(any(MailSettingsImpl.class));
		verify(decorated).add(entity);
		verifyNoMoreInteractions(decorated);
	}

	@Test
	public void testAddValidSettings()
	{
		when(entity.getBoolean(MailSettingsImpl.Meta.TEST_CONNECTION)).thenReturn(true);
		when(entity.getString(MailSettingsImpl.Meta.USERNAME)).thenReturn("Username");
		when(entity.getString(MailSettingsImpl.Meta.PASSWORD)).thenReturn("password");

		mailSettingsRepositoryDecorator.add(entity);
		verify(mailSenderFactory).validateConnection(any(MailSettingsImpl.class));
		verify(decorated).add(entity);
		verifyNoMoreInteractions(decorated);
	}

	@Test
	public void testAddInvalidSettings()
	{
		when(entity.getBoolean(MailSettingsImpl.Meta.TEST_CONNECTION)).thenReturn(true);
		when(entity.getString(MailSettingsImpl.Meta.USERNAME)).thenReturn("Username");
		when(entity.getString(MailSettingsImpl.Meta.PASSWORD)).thenReturn("password");

		doThrow(IllegalStateException.class).when(mailSenderFactory).validateConnection(any(MailSettingsImpl.class));

		try
		{
			mailSettingsRepositoryDecorator.add(entity);
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
		when(entity.getBoolean(MailSettingsImpl.Meta.TEST_CONNECTION)).thenReturn(testConnection);
		when(entity.getString(MailSettingsImpl.Meta.USERNAME)).thenReturn(username);
		when(entity.getString(MailSettingsImpl.Meta.PASSWORD)).thenReturn(password);
		mailSettingsRepositoryDecorator.update(entity);
		verify(mailSenderFactory, never()).validateConnection(any(MailSettingsImpl.class));
		verify(decorated).update(entity);
		verifyNoMoreInteractions(decorated);
	}

	@Test
	public void testUpdateValidSettings()
	{
		when(entity.getBoolean(MailSettingsImpl.Meta.TEST_CONNECTION)).thenReturn(true);
		when(entity.getString(MailSettingsImpl.Meta.USERNAME)).thenReturn("Username");
		when(entity.getString(MailSettingsImpl.Meta.PASSWORD)).thenReturn("password");

		mailSettingsRepositoryDecorator.update(entity);
		verify(mailSenderFactory).validateConnection(any(MailSettingsImpl.class));
		verify(decorated).update(entity);
		verifyNoMoreInteractions(decorated);
	}

	@Test
	public void testUpdateInvalidSettings()
	{
		when(entity.getBoolean(MailSettingsImpl.Meta.TEST_CONNECTION)).thenReturn(true);
		when(entity.getString(MailSettingsImpl.Meta.USERNAME)).thenReturn("Username");
		when(entity.getString(MailSettingsImpl.Meta.PASSWORD)).thenReturn("password");

		doThrow(IllegalStateException.class).when(mailSenderFactory).validateConnection(any(MailSettingsImpl.class));

		try
		{
			mailSettingsRepositoryDecorator.update(entity);
			Assert.fail("Should've thrown exception.");
		}
		catch (IllegalStateException expected)
		{
			verifyZeroInteractions(decorated);
		}
	}

}
