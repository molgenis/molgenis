package org.molgenis.jobs;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.jobs.config.JobTestConfig;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.jobs.model.JobExecutionLogAppender;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { ProgressImplTest.Config.class })
public class ProgressImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private JobExecutionMetaData jobExecutionMeta;

	private ProgressImpl progress;
	@Mock
	private JobExecutionUpdater updater;
	@Mock
	private MailSender mailSender;
	private JobExecution jobExecution;

	@BeforeMethod
	public void beforeMethod()
	{
		jobExecution = new JobExecution(jobExecutionMeta)
		{
		};
		jobExecution.setIdentifier("ABCDE");
		jobExecution.setType("Annotator");
		progress = new ProgressImpl(jobExecution, updater, mailSender);
	}

	@Test
	public void testLog()
	{
		progress.start();
		progress.status("Working....");
		progress.success();
		System.out.println(jobExecution.getLog());
		assertTrue(jobExecution.getLog().contains("- Execution started." + System.lineSeparator()));
		assertTrue(jobExecution.getLog().contains("- Working...." + System.lineSeparator()));
		assertTrue(jobExecution.getLog().contains("- Execution successful. Time spent: "));
	}

	@Test
	public void testMailSuccess()
	{
		jobExecution.setSuccessEmail("a@b.c,d@e.f");
		progress.start();
		progress.status("Working....");
		progress.success();
		System.out.println(jobExecution.getLog());
		assertTrue(jobExecution.getLog().contains("- Execution started." + System.lineSeparator()));
		assertTrue(jobExecution.getLog().contains("- Working...." + System.lineSeparator()));
		assertTrue(jobExecution.getLog().contains("- Execution successful. Time spent: "));

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(new String[] { "a@b.c", "d@e.f" });
		mail.setSubject("Annotator job succeeded.");
		mail.setText(jobExecution.getLog());
		Mockito.verify(mailSender).send(mail);
	}

	@Test
	public void testMailFailed()
	{
		jobExecution.setFailureEmail("a@b.c,d@e.f");
		progress.start();
		progress.status("Working....");
		Exception ex = new IllegalArgumentException("blah");
		progress.failed(ex);
		System.out.println(jobExecution.getLog());
		assertTrue(jobExecution.getLog().contains("- Execution started." + System.lineSeparator()));
		assertTrue(jobExecution.getLog().contains("- Working...." + System.lineSeparator()));
		assertTrue(jobExecution.getLog().contains("- Failed"));
		assertTrue(jobExecution.getLog().contains(ex.getMessage()));

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(new String[] { "a@b.c", "d@e.f" });
		mail.setSubject("Annotator job failed.");
		mail.setText(jobExecution.getLog());
		Mockito.verify(mailSender).send(mail);
	}

	@Test
	public void jobSucceedsButMailFails()
	{
		doThrow(new MailPreparationException("fail!")).when(mailSender).send(any(SimpleMailMessage.class));
		jobExecution.setProgressMessage("Job finished.");
		jobExecution.setSuccessEmail("test@test");
		progress.start();
		progress.success();

		Mockito.verify(mailSender).send(any(SimpleMailMessage.class));
		assertEquals(jobExecution.getProgressMessage(), "Job finished. (Mail not sent: fail!)");
	}

	@Test
	public void jobFailsAndMailFails()
	{
		doThrow(new MailPreparationException("fail!")).when(mailSender).send(any(SimpleMailMessage.class));
		jobExecution.setProgressMessage("Downloading...");
		jobExecution.setFailureEmail("test@test");
		progress.start();

		String exceptionMessage = "x is not a number";
		Exception ex = new IllegalArgumentException(exceptionMessage);
		progress.failed(ex);

		Mockito.verify(mailSender).send(any(SimpleMailMessage.class));
		assertEquals(jobExecution.getProgressMessage(), exceptionMessage + " (Mail not sent: fail!)");
	}

	@Test
	public void jobCanceledAndMailFails()
	{
		doThrow(new MailPreparationException("fail!")).when(mailSender).send(any(SimpleMailMessage.class));
		jobExecution.setProgressMessage("Downloading...");
		jobExecution.setFailureEmail("test@test");
		progress.start();
		progress.canceled();

		Mockito.verify(mailSender).send(any(SimpleMailMessage.class));
		assertEquals(jobExecution.getProgressMessage(), "Downloading... (Mail not sent: fail!)");
	}

	@Configuration
	@Import(JobTestConfig.class)
	public static class Config
	{
		public Config()
		{
			Logger logger = LoggerFactory.getLogger(JobExecution.class);
			if (!(logger instanceof ch.qos.logback.classic.Logger))
			{
				throw new RuntimeException("Expected logback als SLF4J implementation");
			}
			ch.qos.logback.classic.Logger jobExecutionLog = (ch.qos.logback.classic.Logger) logger;
			jobExecutionLog.setLevel(Level.ALL);
			JobExecutionLogAppender appender = new JobExecutionLogAppender();
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			appender.setContext(lc);
			appender.start();
			jobExecutionLog.addAppender(appender);
		}

	}
}
