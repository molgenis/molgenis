package org.molgenis.data.jobs.model.hello;

import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.jobs.JobExecutionConfig;
import org.molgenis.data.jobs.JobExecutor;
import org.molgenis.data.jobs.JobFactoryRegistry;
import org.molgenis.data.jobs.config.JobTestConfig;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailSender;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { JobTestConfig.class, JobExecutionConfig.class, HelloWorldJobExecutionFactory.class,
		HelloWorldJobExecutionMetadata.class, JobExecutionMetaData.class, HelloWorldConfig.class, JobExecutor.class,
		EntityManagerImpl.class, HelloWorldDemo.Config.class, JobFactoryRegistry.class })
public class HelloWorldDemo extends AbstractMolgenisSpringTest
{
	@Autowired
	JobExecutor jobExecutor;

	@Autowired
	HelloWorldJobExecutionFactory factory;

	@Test
	public void helloWorld() throws InterruptedException
	{
		HelloWorldJobExecution jobExecution = factory.create();
		jobExecution.setDelay(1);
		jobExecution.setUser("user");
		jobExecutor.submit(jobExecution);
		Thread.sleep(1100);
		assertTrue(jobExecution.getLog().contains("Hello user!"));
	}

	public static class Config
	{
		@Mock
		private MailSender mailSender;

		public Config()
		{
			initMocks(this);
		}

		@Bean
		public MailSender mailSender()
		{
			return mailSender;
		}
	}
}
