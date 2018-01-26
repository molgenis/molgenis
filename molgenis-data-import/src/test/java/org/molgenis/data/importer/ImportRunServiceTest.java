package org.molgenis.data.importer;

import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.importer.config.ImportTestConfig;
import org.molgenis.data.security.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.importer.ImportStatus.FAILED;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { ImportTestConfig.class, ImportRunService.class, ImportRunServiceTest.Config.class })
public class ImportRunServiceTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private ImportRunService importRunService;

	@Mock
	private ImportRun importRun;

	@BeforeMethod
	public void setUp() throws Exception
	{
		initMocks(this);
	}

	@Test
	public void testCreateEnglishMailText()
	{
		when(importRun.getMessage()).thenReturn("Entity already exists.");
		when(importRun.getStatus()).thenReturn(FAILED.toString());
		Instant startDate = Instant.parse("2016-02-13T12:34:56.217Z");
		when(importRun.getStartDate()).thenReturn(startDate);
		Instant endDate = Instant.parse("2016-02-13T12:35:12.231Z");
		when(importRun.getEndDate()).thenReturn(endDate);

		String mailText = importRunService.createEnglishMailText(importRun, ZoneId.of("Europe/Amsterdam"));
		assertEquals(mailText, "The import started by you on Saturday, February 13, 2016 1:34:56 PM CET "
				+ "finished on 1:35:12 PM with status: FAILED\nMessage:\nEntity already exists.");
	}

	@Configuration
	public static class Config
	{
		@Bean
		MailSender mailSender()
		{
			return mock(MailSender.class);
		}

		@Bean
		UserService userService()
		{
			return mock(UserService.class);
		}
	}

}