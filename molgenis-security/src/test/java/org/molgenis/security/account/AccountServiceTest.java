package org.molgenis.security.account;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.Arrays;

import javax.mail.internet.MimeMessage;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
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
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{

			MolgenisSettings molgenisSettings = mock(MolgenisSettings.class);
			when(molgenisSettings.getProperty("plugin.auth.activation_mode")).thenReturn("user");
			return molgenisSettings;
		}

		@Bean
		public PasswordEncoder passwordEncoder()
		{
			return mock(PasswordEncoder.class);
		}

		@Bean
		public JavaMailSender mailSender()
		{
			return mock(JavaMailSender.class);
		}

		@Bean
		public MolgenisUserService molgenisUserService()
		{
			return mock(MolgenisUserService.class);
		}
	}

	@Autowired
	private AccountService accountService;

	@Autowired
	private DataService dataService;

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeMethod
	public void setUp()
	{
		reset(dataService);

		MolgenisGroup allUsersGroup = mock(MolgenisGroup.class);
		when(
				dataService.findAllAsList(MolgenisGroup.ENTITY_NAME,
						new QueryImpl().eq(MolgenisGroup.NAME, AccountService.ALL_USER_GROUP))).thenReturn(
				Arrays.<Entity> asList(allUsersGroup));
		reset(javaMailSender);
		MimeMessage mimeMessage = mock(MimeMessage.class);
		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
	}

	@Test
	public void activateUser()
	{
		when(
				dataService.findOne(MolgenisUser.ENTITY_NAME,
						new QueryImpl().eq(MolgenisUser.ACTIVE, false).eq(MolgenisUser.ACTIVATIONCODE, "123")))
				.thenReturn(new MolgenisUser());

		accountService.activateUser("123");

		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
		verify(dataService).update(eq(MolgenisUser.ENTITY_NAME), argument.capture());
		assertTrue(argument.getValue().getActive());
		verify(javaMailSender).send(any(SimpleMailMessage.class));
		// TODO improve test
	}

	@Test(expectedExceptions = MolgenisUserException.class)
	public void activateUser_invalidActivationCode()
	{
		accountService.activateUser("invalid");
	}

	@Test(expectedExceptions = MolgenisUserException.class)
	public void activateUser_alreadyActivated()
	{
		when(
				dataService.findOne(MolgenisUser.ENTITY_NAME,
						new QueryImpl().eq(MolgenisUser.ACTIVE, false).eq(MolgenisUser.ACTIVATIONCODE, "456")))
				.thenReturn(null);

		accountService.activateUser("456");
	}

	@Test
	public void createUser() throws URISyntaxException
	{
		MolgenisUser molgenisUser = new MolgenisUser();
		molgenisUser.setEmail("user@molgenis.org");
		accountService.createUser(molgenisUser, "http://molgenis.org/activate");
		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
		verify(dataService).add(eq(MolgenisUser.ENTITY_NAME), argument.capture());
		assertFalse(argument.getValue().getActive());
		verify(javaMailSender).send(any(SimpleMailMessage.class));
		// TODO improve test
	}

	@Test
	public void resetPassword()
	{
		MolgenisUser molgenisUser = mock(MolgenisUser.class);
		when(molgenisUser.getPassword()).thenReturn("password");
		when(dataService.findOne(eq(MolgenisUser.ENTITY_NAME), any(Query.class))).thenReturn(molgenisUser);

		accountService.resetPassword("user@molgenis.org");
		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
		verify(passwordEncoder).encode(any(String.class));
		verify(dataService).update(eq(MolgenisUser.ENTITY_NAME), argument.capture());
		assertNotNull(argument.getValue().getPassword());
		verify(javaMailSender).send(any(SimpleMailMessage.class));
		// TODO improve test
	}

	@Test(expectedExceptions = MolgenisUserException.class)
	public void resetPassword_invalidEmailAddress()
	{
		MolgenisUser molgenisUser = mock(MolgenisUser.class);
		when(molgenisUser.getPassword()).thenReturn("password");
		when(
				dataService.findOne(MolgenisUser.ENTITY_NAME,
						new QueryImpl().eq(MolgenisUser.EMAIL, "invalid-user@molgenis.org"))).thenReturn(null);

		accountService.resetPassword("invalid-user@molgenis.org");
	}
}
