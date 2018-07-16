package org.molgenis.jobs;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.security.token.RunAsUserTokenFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailSender;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Objects.requireNonNull;

@Import(JobFactoryRegistry.class)
@Configuration
public class JobExecutionConfig
{
	private final DataService dataService;
	private final EntityManager entityManager;
	private final UserDetailsService userDetailsService;
	private final JobExecutionUpdater jobExecutionUpdater;
	private final MailSender mailSender;
	private final JobFactoryRegistry jobFactoryRegistry;
	private final RunAsUserTokenFactory runAsUserTokenFactory;

	@Autowired
	private ExecutorService executorService;

	public JobExecutionConfig(DataService dataService, EntityManager entityManager,
			UserDetailsService userDetailsService, JobExecutionUpdater jobExecutionUpdater, MailSender mailSender,
			JobFactoryRegistry jobFactoryRegistry, RunAsUserTokenFactory runAsUserTokenFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.entityManager = requireNonNull(entityManager);
		this.userDetailsService = requireNonNull(userDetailsService);
		this.jobExecutionUpdater = requireNonNull(jobExecutionUpdater);
		this.mailSender = requireNonNull(mailSender);
		this.jobFactoryRegistry = requireNonNull(jobFactoryRegistry);
		this.runAsUserTokenFactory = requireNonNull(runAsUserTokenFactory);
	}

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
				executorService, jobFactoryRegistry, runAsUserTokenFactory);
	}
}
