package org.molgenis.data.jobs;

import static org.testng.Assert.assertTrue;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProgressImplTest
{
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
		jobExecution = new JobExecution(null);
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
		assertTrue(jobExecution.getLog().contains("INFO  - start ()" + System.lineSeparator()));
		assertTrue(jobExecution.getLog().contains("INFO  - Working...." + System.lineSeparator()));
		assertTrue(jobExecution.getLog().contains("INFO  - Execution successful. Time spent: "));
	}

	@Test
	public void testMailSuccess()
	{
		jobExecution.setSuccessEmail("a@b.c,d@e.f");
		progress.start();
		progress.status("Working....");
		progress.success();
		System.out.println(jobExecution.getLog());
		assertTrue(jobExecution.getLog().contains("INFO  - start ()" + System.lineSeparator()));
		assertTrue(jobExecution.getLog().contains("INFO  - Working...." + System.lineSeparator()));
		assertTrue(jobExecution.getLog().contains("INFO  - Execution successful. Time spent: "));

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(new String[]
		{ "a@b.c", "d@e.f" });
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
		assertTrue(jobExecution.getLog().contains("INFO  - start ()" + System.lineSeparator()));
		assertTrue(jobExecution.getLog().contains("INFO  - Working...." + System.lineSeparator()));
		assertTrue(jobExecution.getLog().contains("ERROR - Failed"));
		assertTrue(jobExecution.getLog().contains(ex.getMessage()));

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(new String[]
		{ "a@b.c", "d@e.f" });
		mail.setSubject("Annotator job failed.");
		mail.setText(jobExecution.getLog());
		Mockito.verify(mailSender).send(mail);
	}
}
