package org.molgenis.omx.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Part;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.filter.StudyDataRequest;
import org.molgenis.omx.observ.ObservableFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration
public class OrderStudyDataServiceTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public OrderStudyDataService orderStudyDataService()
		{
			return new OrderStudyDataService();
		}

		@Bean
		public Database database() throws DatabaseException
		{
			ObservableFeature feature0 = mock(ObservableFeature.class);
			when(feature0.getId()).thenReturn(0);
			when(feature0.getName()).thenReturn("feature #0");
			when(feature0.getDescription()).thenReturn("feature #0 description");

			ObservableFeature feature1 = mock(ObservableFeature.class);
			when(feature1.getId()).thenReturn(1);
			when(feature1.getName()).thenReturn("feature #1");
			when(feature1.getDescription()).thenReturn("feature #1 description");

			Database database = mock(Database.class);
			when(
					database.find(
							ObservableFeature.class,
							new QueryRule(ObservableFeature.ID, Operator.IN, Arrays.asList(Integer.valueOf(0),
									Integer.valueOf(1))))).thenReturn(Arrays.asList(feature0, feature1));

			MolgenisUser molgenisUser = mock(MolgenisUser.class);
			when(molgenisUser.getEmail()).thenReturn("test@molgenis.org");
			when(database.findById(MolgenisUser.class, 1)).thenReturn(molgenisUser);
			return database;
		}

		@Bean
		public Login login()
		{
			Login login = mock(Login.class);
			when(login.getUserId()).thenReturn(1);
			return login;
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}

		@Bean
		public JavaMailSender mailSender()
		{
			MimeMessage mimeMessage = mock(MimeMessage.class);
			JavaMailSender javaMailSender = mock(JavaMailSender.class);
			when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
			return javaMailSender;
		}
	}

	@Autowired
	private OrderStudyDataService orderStudyDataService;

	@Autowired
	private Database database;

	@Autowired
	private JavaMailSender javaMailSender;

	@Test
	public void orderStudyData() throws DatabaseException, MessagingException, IOException
	{
		Part requestForm = mock(Part.class);
		when(requestForm.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]
		{ 0, 1, 2 }));
		orderStudyDataService.orderStudyData("study #1", requestForm,
				Arrays.asList(Integer.valueOf(0), Integer.valueOf(1)));

		// TODO improve test
		verify(database).add(any(StudyDataRequest.class));
		verify(javaMailSender).send(any(MimeMessage.class));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void orderStudyData_noStudyName() throws DatabaseException, MessagingException, IOException
	{
		orderStudyDataService.orderStudyData(null, mock(Part.class),
				Arrays.asList(Integer.valueOf(0), Integer.valueOf(1)));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void orderStudyData_noRequestForm() throws DatabaseException, MessagingException, IOException
	{
		orderStudyDataService.orderStudyData("study #1", null, Arrays.asList(Integer.valueOf(0), Integer.valueOf(1)));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void orderStudyData_noFeatures() throws DatabaseException, MessagingException, IOException
	{
		orderStudyDataService.orderStudyData("study #1", mock(Part.class), null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void orderStudyData_emptyFeatures() throws DatabaseException, MessagingException, IOException
	{
		orderStudyDataService.orderStudyData("study #1", mock(Part.class), Collections.<Integer> emptyList());
	}

	@Test(expectedExceptions = DatabaseException.class)
	public void orderStudyData_invalidFeatures() throws DatabaseException, MessagingException, IOException
	{
		orderStudyDataService.orderStudyData("study #1", mock(Part.class),
				Arrays.asList(Integer.valueOf(-2), Integer.valueOf(-1)));
	}
}
