package org.molgenis.data.jobs.schedule;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.JobExecutionTemplate;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.jobs.model.ScheduledJob;
import org.molgenis.data.meta.model.EntityType;
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
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
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

	private DataService dataService;
	//TODO: have the factory decide which bean to create depending on the job type
	private Function<JobExecution, ? extends Job> jobFactory;

	private EntityManager entityManager;
	private Gson gson;
	private JobExecutionTemplate jobExecutionTemplate;
	private JobExecutionUpdater jobExecutionUpdater;
	private MailSender mailSender;
	private UserDetailsService userDetailsService;

	@Autowired
	public JobExecutor(DataService dataService, Function<JobExecution, ? extends Job> jobFactory,
			EntityManager entityManager, Gson gson, JobExecutionTemplate jobExecutionTemplate,
			UserDetailsService userDetailsService, JobExecutionUpdater jobExecutionUpdater, MailSender mailSender)
	{
		this.dataService = requireNonNull(dataService);
		this.jobFactory = requireNonNull(jobFactory);
		this.entityManager = requireNonNull(entityManager);
		this.gson = requireNonNull(gson);
		this.jobExecutionTemplate = requireNonNull(jobExecutionTemplate);
		this.userDetailsService = requireNonNull(userDetailsService);
		this.jobExecutionUpdater = requireNonNull(jobExecutionUpdater);
		this.mailSender = requireNonNull(mailSender);
	}

	@RunAsSystem
	public void executeScheduledJob(String scheduledJobId)
	{
		ScheduledJob scheduledJob = dataService.findOneById(SCHEDULED_JOB, scheduledJobId, ScheduledJob.class);
		JobExecution jobExecution = createJobExecution(scheduledJob);
		Job<?> molgenisJob = jobFactory.apply(jobExecution);
		runJob(jobExecution, molgenisJob);
	}

	private JobExecution createJobExecution(ScheduledJob scheduledJob)
	{
		EntityType jobExecutionEntityType = getJobExecutionEntityTypeName(scheduledJob);
		JobExecution jobExecution = (JobExecution) entityManager.create(jobExecutionEntityType, POPULATE);
		jobExecution.setDefaultValues();
		writePropertyValues(jobExecution, getPropertyValues(scheduledJob.getParameters()));
		jobExecution.setFailureEmail(scheduledJob.getFailureEmail());
		jobExecution.setSuccessEmail(scheduledJob.getSuccessEmail());
		jobExecution.setUser(scheduledJob.getUser());
		dataService.add(jobExecution.getEntityType().getId(), jobExecution);
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

	private EntityType getJobExecutionEntityTypeName(ScheduledJob scheduledJob)
	{
		// TODO where and how?!?
		return dataService.getEntityType("sys_FileIngestJobExecution");
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
