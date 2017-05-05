package org.molgenis.data.jobs.schedule;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.jobs.model.ScheduledJob;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Map;

import static java.util.Objects.requireNonNull;
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
	//TODO: determine which exact factory based on the ScheduledJob
	private MolgenisJobFactory jobFactory;
	private EntityManager entityManager;
	private Gson gson;

	public JobExecutor(DataService dataService, MolgenisJobFactory jobFactory, EntityManager entityManager, Gson gson)
	{
		this.dataService = requireNonNull(dataService);
		this.jobFactory = requireNonNull(jobFactory);
		this.entityManager = requireNonNull(entityManager);
		this.gson = requireNonNull(gson);
	}

	@RunAsSystem
	public void execute(String scheduledJobId)
	{
		ScheduledJob scheduledJob = dataService.findOneById(SCHEDULED_JOB, scheduledJobId, ScheduledJob.class);

		JobExecution jobExecution = (JobExecution) entityManager
				.create(jobFactory.getJobExecutionType(), EntityManager.CreationMode.POPULATE);
		jobExecution.setDefaultValues();
		writePropertyValues(jobExecution, getPropertyValues(scheduledJob.getParameters()));
		jobExecution.setFailureEmail(scheduledJob.getFailureEmail());
		jobExecution.setSuccessEmail(scheduledJob.getSuccessEmail());
		jobExecution.setUser(scheduledJob.getUser());

		org.molgenis.data.jobs.Job molgenisJob = jobFactory.createJob(jobExecution);
		molgenisJob.call();
	}

	private void writePropertyValues(JobExecution jobExecution, MutablePropertyValues pvs)
	{
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(jobExecution);
		bw.setPropertyValues(pvs, true);
	}

	private MutablePropertyValues getPropertyValues(String scheduledJobParameters)
	{
		Map<String, Object> parameters = gson.fromJson(scheduledJobParameters, MAP_TOKEN);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValues(parameters);
		return pvs;
	}
}
