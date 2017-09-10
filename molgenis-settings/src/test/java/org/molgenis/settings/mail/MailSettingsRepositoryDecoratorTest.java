package org.molgenis.settings.mail;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.util.mail.MailSenderFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class MailSettingsRepositoryDecoratorTest
{
	private MailSettingsRepositoryDecorator mailSettingsRepositoryDecorator;
	@Mock
	private Entity entity;
	@Mock
	private MailSenderFactory mailSenderFactory;
	@Mock
	private Repository<Entity> delegateRepository;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
		mailSettingsRepositoryDecorator = new MailSettingsRepositoryDecorator(delegateRepository, mailSenderFactory);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		Mockito.reset(entity, mailSenderFactory, delegateRepository);
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
		verify(delegateRepository).add(entity);
		verifyNoMoreInteractions(delegateRepository);
	}

	@Test
	public void testAddValidSettings()
	{
		when(entity.getBoolean(MailSettingsImpl.Meta.TEST_CONNECTION)).thenReturn(true);
		when(entity.getString(MailSettingsImpl.Meta.USERNAME)).thenReturn("Username");
		when(entity.getString(MailSettingsImpl.Meta.PASSWORD)).thenReturn("password");

		mailSettingsRepositoryDecorator.add(entity);
		verify(mailSenderFactory).validateConnection(any(MailSettingsImpl.class));
		verify(delegateRepository).add(entity);
		verifyNoMoreInteractions(delegateRepository);
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
			verifyZeroInteractions(delegateRepository);
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
		verify(delegateRepository).update(entity);
		verifyNoMoreInteractions(delegateRepository);
	}

	@Test
	public void testUpdateValidSettings()
	{
		when(entity.getBoolean(MailSettingsImpl.Meta.TEST_CONNECTION)).thenReturn(true);
		when(entity.getString(MailSettingsImpl.Meta.USERNAME)).thenReturn("Username");
		when(entity.getString(MailSettingsImpl.Meta.PASSWORD)).thenReturn("password");

		mailSettingsRepositoryDecorator.update(entity);
		verify(mailSenderFactory).validateConnection(any(MailSettingsImpl.class));
		verify(delegateRepository).update(entity);
		verifyNoMoreInteractions(delegateRepository);
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
			verifyZeroInteractions(delegateRepository);
		}
	}

}
