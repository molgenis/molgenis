package org.molgenis.omx.auth.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import javax.mail.internet.MimeMessage;

import org.mockito.ArgumentCaptor;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.auth.MolgenisUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration
public class AccountServiceTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public AccountService accountService()
		{
			return new AccountService();
		}

		@Bean
		@Qualifier("unauthorizedDatabase")
		public Database unauthorizedDatabase() throws DatabaseException
		{
			return mock(Database.class);
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{

			MolgenisSettings molgenisSettings = mock(MolgenisSettings.class);
			when(molgenisSettings.getProperty("plugin.auth.activation_mode")).thenReturn("user");
			return molgenisSettings;
		}

		@Bean
		public JavaMailSender mailSender()
		{
			return mock(JavaMailSender.class);
		}
	}

	@Autowired
	private AccountService accountService;

	@Autowired
	@Qualifier("unauthorizedDatabase")
	private Database unauthorizedDatabase;

	@Autowired
	private JavaMailSender javaMailSender;

	@BeforeMethod
	public void setUp() throws DatabaseException
	{
		reset(unauthorizedDatabase);
		when(
				unauthorizedDatabase.find(MolgenisUser.class,
						new QueryRule(MolgenisUser.ACTIVE, Operator.EQUALS, false), new QueryRule(
								MolgenisUser.ACTIVATIONCODE, Operator.EQUALS, "123"))).thenReturn(
				Collections.<MolgenisUser> singletonList(new MolgenisUser()));
		when(
				unauthorizedDatabase.find(MolgenisUser.class,
						new QueryRule(MolgenisUser.ACTIVE, Operator.EQUALS, false), new QueryRule(
								MolgenisUser.ACTIVATIONCODE, Operator.EQUALS, "456"))).thenReturn(
				Collections.<MolgenisUser> emptyList());

		reset(javaMailSender);
		MimeMessage mimeMessage = mock(MimeMessage.class);
		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
	}

	@Test
	public void activateUser() throws DatabaseException
	{
		accountService.activateUser("123");

		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
		verify(unauthorizedDatabase).update(argument.capture());
		assertTrue(argument.getValue().getActive());
		verify(javaMailSender).send(any(SimpleMailMessage.class));
		// TODO improve test
	}

	@SuppressWarnings("unchecked")
	@Test
	public void activateUser_invalidActivationCode() throws DatabaseException
	{
		accountService.activateUser("invalid");
		verify(unauthorizedDatabase).find(any(Class.class), any(QueryRule.class), any(QueryRule.class));
		verifyNoMoreInteractions(unauthorizedDatabase);
		verifyNoMoreInteractions(javaMailSender);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void activateUser_alreadyActivated() throws DatabaseException
	{
		accountService.activateUser("456");
		verify(unauthorizedDatabase).find(any(Class.class), any(QueryRule.class), any(QueryRule.class));
		verifyNoMoreInteractions(unauthorizedDatabase);
		verifyNoMoreInteractions(javaMailSender);
	}

	@Test
	public void createUser() throws DatabaseException, URISyntaxException
	{
		MolgenisUser molgenisUser = new MolgenisUser();
		molgenisUser.setEmail("user@molgenis.org");
		accountService.createUser(molgenisUser, new URI("http://molgenis.org/activate"));
		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
		verify(unauthorizedDatabase).add(argument.capture());
		assertFalse(argument.getValue().getActive());
		verify(javaMailSender).send(any(SimpleMailMessage.class));
		// TODO improve test
	}

	@Test
	public void resetPassword() throws DatabaseException
	{
		accountService.resetPassword(new MolgenisUser());
		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
		verify(unauthorizedDatabase).update(argument.capture());
		assertNotNull(argument.getValue().getPassword());
		verify(javaMailSender).send(any(SimpleMailMessage.class));
		// TODO improve test
	}
}
