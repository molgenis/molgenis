package org.molgenis.data.jobs.schedule;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.jobs.*;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.jobs.model.ScheduledJob;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.jobs.model.JobTypeMetadata.JOB_TYPE;
import static org.molgenis.data.jobs.model.ScheduledJobMetadata.SCHEDULED_JOB;

/**
 * Executes {@link ScheduledJob}s.
 */
@Service
public class JobExecutor
{
	private static final Type MAP_TOKEN = new TypeToken<Map<String, Object>>()
	{
	}.getType();

	private final DataService dataService;
	private final EntityManager entityManager;
	private final Gson gson;
	private final JobExecutionTemplate jobExecutionTemplate;
	private final JobExecutionUpdater jobExecutionUpdater;
	private final MailSender mailSender;
	private final UserDetailsService userDetailsService;
	private final List<JobFactory> jobFactories;

	@Autowired
	public JobExecutor(DataService dataService, List<JobFactory> jobFactories, EntityManager entityManager, Gson gson,
			JobExecutionTemplate jobExecutionTemplate, UserDetailsService userDetailsService,
			JobExecutionUpdater jobExecutionUpdater, MailSender mailSender)
	{
		this.dataService = requireNonNull(dataService);
		this.entityManager = requireNonNull(entityManager);
		this.gson = requireNonNull(gson);
		this.jobExecutionTemplate = requireNonNull(jobExecutionTemplate);
		this.userDetailsService = requireNonNull(userDetailsService);
		this.jobExecutionUpdater = requireNonNull(jobExecutionUpdater);
		this.mailSender = requireNonNull(mailSender);
		this.jobFactories = jobFactories;
	}

	private JobFactory getJobFactoryForType(String jobExecutionTypeId)
	{
		return jobFactories.stream()
				.filter(f -> f.getJobType().getJobExecutionType().getId().equals(jobExecutionTypeId)).findFirst().get();
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
		execute(jobExecution);
	}

	/**
	 * Executes a JobExecution in the current thread.
	 *
	 * @param jobExecution the {@link JobExecution} to execute.
	 */
	@RunAsSystem
	public void execute(JobExecution jobExecution)
	{
		dataService.add(jobExecution.getEntityType().getId(), jobExecution);
		JobFactory jobFactory = getJobFactoryForType(jobExecution.getEntityType().getId());
		Job molgenisJob = jobFactory.createJob(jobExecution);
		runJob(jobExecution, molgenisJob);
	}

	private JobExecution createJobExecution(ScheduledJob scheduledJob)
	{
		JobExecution jobExecution = (JobExecution) entityManager
				.create(scheduledJob.getType().getJobExecutionType(), POPULATE);
		writePropertyValues(jobExecution, getPropertyValues(scheduledJob.getParameters()));
		jobExecution.setFailureEmail(scheduledJob.getFailureEmail());
		jobExecution.setSuccessEmail(scheduledJob.getSuccessEmail());
		jobExecution.setUser(scheduledJob.getUser());
		return jobExecution;
	}

	private void runJob(JobExecution jobExecution, Job<?> molgenisJob)
	{
		jobExecutionTemplate.call(molgenisJob, new ProgressImpl(jobExecution, jobExecutionUpdater, mailSender),
				createAuthorization(jobExecution.getUser()));
	}

	private RunAsUserToken createAuthorization(String username)
	{
		return new RunAsUserToken("JobImpl Execution", username, null,
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

	public void upsertJobTypes()
	{
		dataService.getRepository(JOB_TYPE)
				.upsertBatch(jobFactories.stream().map(JobFactory::getJobType).collect(toList()));

	}
}
