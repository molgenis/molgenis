package org.molgenis.data.jobs;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

	@BeforeClass
	public void beforeClass()
	{
		MockitoAnnotations.initMocks(this);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		jobExecution = new JobExecution(jobExecutionMeta)
		{
		};
		jobExecution.setIdentifier("ABCDE");
		jobExecution.setType("Annotator");
		progress = new ProgressImpl(jobExecution, updater, () -> mailSender);
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

	@Configuration
	@ComponentScan({ "org.molgenis.data.populate", "org.molgenis.data.jobs.model" })
	public static class Config
	{

	}
}
