package org.molgenis.jobs;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailSender;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Import(JobFactoryRegistry.class)
@Configuration
public class JobExecutionConfig
{
	@Autowired
	private DataService dataService;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private JobExecutionUpdater jobExecutionUpdater;
	@Autowired
	private MailSender mailSender;
	@Autowired
	private ExecutorService executorService;
	@Autowired
	private JobFactoryRegistry jobFactoryRegistry;

	@Primary // Use this ExecutorService when no specific bean is demanded
	@Bean
	public ExecutorService executorService()
	{
		return Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("molgenis-job-%d").build());
	}

	@Bean
	public JobExecutor jobExecutor()
	{
		return new JobExecutor(dataService, entityManager, userDetailsService, jobExecutionUpdater, mailSender,
				executorService, jobFactoryRegistry);
	}
}
