package org.molgenis.jobs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.jobs.model.ScheduledJob;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.mail.MailSender;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.jobs.model.ScheduledJobMetadata.SCHEDULED_JOB;

/**
 * Executes {@link ScheduledJob}s.
 */
@Service
public class JobExecutor
{
	private static final Type MAP_TOKEN = new TypeToken<Map<String, Object>>()
	{
	}.getType();
	private static final Logger LOG = LoggerFactory.getLogger(JobExecutor.class);

	private final DataService dataService;
	private final EntityManager entityManager;
	private final JobExecutionTemplate jobExecutionTemplate = new JobExecutionTemplate();
	private final JobExecutionUpdater jobExecutionUpdater;
	private final MailSender mailSender;
	private final UserDetailsService userDetailsService;
	private final ExecutorService executorService;
	private final JobFactoryRegistry jobFactoryRegistry;
	private final Gson gson;

	public JobExecutor(DataService dataService, EntityManager entityManager, UserDetailsService userDetailsService,
			JobExecutionUpdater jobExecutionUpdater, MailSender mailSender, ExecutorService executorService,
			JobFactoryRegistry jobFactoryRegistry)
	{
		this.dataService = requireNonNull(dataService);
		this.entityManager = requireNonNull(entityManager);
		this.userDetailsService = requireNonNull(userDetailsService);
		this.jobExecutionUpdater = requireNonNull(jobExecutionUpdater);
		this.mailSender = requireNonNull(mailSender);
		this.executorService = requireNonNull(executorService);
		this.jobFactoryRegistry = jobFactoryRegistry;
		this.gson = new Gson();
	}

	/**
	 * Executes a {@link ScheduledJob} in the current thread.
	 *
	 * @param scheduledJobId ID of the {@link ScheduledJob} to run
	 */
	@RunAsSystem
	public void executeScheduledJob(String scheduledJobId)
	{
		ScheduledJob scheduledJob = dataService.findOneById(SCHEDULED_JOB, scheduledJobId, ScheduledJob.class);
		JobExecution jobExecution = createJobExecution(scheduledJob);
		Job molgenisJob = saveExecutionAndCreateJob(jobExecution);

		try
		{
			runJob(jobExecution, molgenisJob);
		}
		catch (Exception ex)
		{
			LOG.error("Error creating job for JobExecution.", ex);
			jobExecution.setStatus(JobExecution.Status.FAILED);
			jobExecution.setProgressMessage(ex.getMessage());
			dataService.update(jobExecution.getEntityType().getId(), jobExecution);
			throw ex;
		}
	}

	private JobExecution createJobExecution(ScheduledJob scheduledJob)
	{
		JobExecution jobExecution = (JobExecution) entityManager.create(scheduledJob.getType().getJobExecutionType(),
				POPULATE);
		writePropertyValues(jobExecution, getPropertyValues(scheduledJob.getParameters()));
		jobExecution.setFailureEmail(scheduledJob.getFailureEmail());
		jobExecution.setSuccessEmail(scheduledJob.getSuccessEmail());
		jobExecution.setUser(scheduledJob.getUser());
		jobExecution.setScheduledJobId(scheduledJob.getId());
		return jobExecution;
	}

	/**
	 * Saves execution in the current thread, then creates a Job and submits that for asynchronous execution.
	 *
	 * @param jobExecution the {@link JobExecution} to save and submit.
	 */
	@RunAsSystem
	public CompletableFuture<Void> submit(JobExecution jobExecution)
	{
		return submit(jobExecution, executorService);
	}

	/**
	 * Saves execution in the current thread, then creates a Job and submits that for asynchronous execution to a
	 * specific ExecutorService.
	 *
	 * @param jobExecution    the {@link JobExecution} to save and submit.
	 * @param executorService the ExecutorService to run the submitted job on
	 */
	@RunAsSystem
	public CompletableFuture<Void> submit(JobExecution jobExecution, ExecutorService executorService)
	{
		Job molgenisJob = saveExecutionAndCreateJob(jobExecution);
		return CompletableFuture.runAsync(() -> runJob(jobExecution, molgenisJob), executorService);
	}

	private Job saveExecutionAndCreateJob(JobExecution jobExecution)
	{
		String entityTypeId = jobExecution.getEntityType().getId();
		dataService.add(entityTypeId, jobExecution);
		try
		{
			JobFactory jobFactory = jobFactoryRegistry.getJobFactory(jobExecution);
			return jobFactory.createJob(jobExecution);
		}
		catch (RuntimeException ex)
		{
			LOG.error("Error creating job for JobExecution.", ex);
			jobExecution.setStatus(JobExecution.Status.FAILED);
			dataService.update(entityTypeId, jobExecution);
			throw ex;
		}
	}

	private void runJob(JobExecution jobExecution, Job<?> molgenisJob)
	{
		jobExecutionTemplate.call(molgenisJob, new ProgressImpl(jobExecution, jobExecutionUpdater, mailSender),
				createAuthorization(jobExecution.getUser()));
	}

	private RunAsUserToken createAuthorization(String username)
	{
		return new RunAsUserToken("Job Execution", username, null,
				userDetailsService.loadUserByUsername(username).getAuthorities(), null);
	}

	private void writePropertyValues(JobExecution jobExecution, MutablePropertyValues pvs)
	{
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(jobExecution);
		bw.setPropertyValues(pvs, true);
	}

	private MutablePropertyValues getPropertyValues(String parameterJson)
	{
		Map<String, Object> parameters = gson.fromJson(parameterJson, MAP_TOKEN);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValues(parameters);
		return pvs;
	}
}
