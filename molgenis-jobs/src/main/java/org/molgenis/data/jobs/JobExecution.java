package org.molgenis.data.jobs;

import static org.molgenis.data.jobs.JobExecutionMetaData.END_DATE;
import static org.molgenis.data.jobs.JobExecutionMetaData.FAILURE_EMAIL;
import static org.molgenis.data.jobs.JobExecutionMetaData.IDENTIFIER;
import static org.molgenis.data.jobs.JobExecutionMetaData.LOG;
import static org.molgenis.data.jobs.JobExecutionMetaData.PROGRESS_INT;
import static org.molgenis.data.jobs.JobExecutionMetaData.PROGRESS_MAX;
import static org.molgenis.data.jobs.JobExecutionMetaData.PROGRESS_MESSAGE;
import static org.molgenis.data.jobs.JobExecutionMetaData.RESULT_URL;
import static org.molgenis.data.jobs.JobExecutionMetaData.START_DATE;
import static org.molgenis.data.jobs.JobExecutionMetaData.STATUS;
import static org.molgenis.data.jobs.JobExecutionMetaData.SUBMISSION_DATE;
import static org.molgenis.data.jobs.JobExecutionMetaData.SUCCESS_EMAIL;
import static org.molgenis.data.jobs.JobExecutionMetaData.TYPE;
import static org.molgenis.data.jobs.JobExecutionMetaData.USER;

import java.util.Date;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;
import org.springframework.util.StringUtils;

/**
 * Superclass that represents a job execution.
 */
public abstract class JobExecution extends StaticEntity
{
	public JobExecution(Entity entity)
	{
		super(entity);
	}

	public JobExecution(EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
	}

	public JobExecution(String identifier, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();

		setIdentifier(identifier);
	}

	public String getIdentifier()
	{
		return getString(IDENTIFIER);
	}

	public void setIdentifier(String value)
	{
		set(IDENTIFIER, value);
	}

	public String getUser()
	{
		return getString(USER);
	}

	public void setUser(String username)
	{
		set(USER, username);
	}

	public void setUser(MolgenisUser value)
	{
		set(USER, value.getUsername());
	}

	public Status getStatus()
	{
		return Status.valueOf(getString(STATUS));
	}

	public void setStatus(Status value)
	{
		set(STATUS, value.toString().toUpperCase());
	}

	public String getType()
	{
		return getString(TYPE);
	}

	public void setType(String value)
	{
		set(TYPE, value);
	}

	public Date getSubmissionDate()
	{
		return getUtilDate(SUBMISSION_DATE);
	}

	public void setSubmissionDate(Date value)
	{
		set(SUBMISSION_DATE, value);
	}

	public Date getStartDate()
	{
		return getUtilDate(START_DATE);
	}

	public void setStartDate(Date value)
	{
		set(START_DATE, value);
	}

	public Date getEndDate()
	{
		return getUtilDate(END_DATE);
	}

	public void setEndDate(Date value)
	{
		set(END_DATE, value);
	}

	public Integer getProgressInt()
	{
		return getInt(PROGRESS_INT);
	}

	public void setProgressInt(Integer value)
	{
		set(PROGRESS_INT, value);
	}

	public String getProgressMessage()
	{
		return getString(PROGRESS_MESSAGE);
	}

	public void setProgressMessage(String value)
	{
		set(PROGRESS_MESSAGE, value);
	}

	public Integer getProgressMax()
	{
		return getInt(PROGRESS_MAX);
	}

	public void setProgressMax(Integer value)
	{
		set(PROGRESS_MAX, value);
	}

	public String getLog()
	{
		return getString(LOG);
	}

	public void setLog(String value)
	{
		set(LOG, value);
	}

	public String getResultUrl()
	{
		return getString(LOG);
	}

	public void setResultUrl(String value)
	{
		set(RESULT_URL, value);
	}

	public String[] getSuccessEmail()
	{
		String email = getString(SUCCESS_EMAIL);
		if (StringUtils.isEmpty(email))
		{
			return new String[] {};
		}
		return email.split(",");
	}

	public String[] getFailureEmail()
	{
		String email = getString(FAILURE_EMAIL);
		if (StringUtils.isEmpty(email))
		{
			return new String[] {};
		}
		return email.split(",");
	}

	public void setSuccessEmail(String successEmail)
	{
		set(SUCCESS_EMAIL, successEmail);
	}

	public void setFailureEmail(String failureEmail)
	{
		set(FAILURE_EMAIL, failureEmail);
	}

	public enum Status
	{
		PENDING, RUNNING, SUCCESS, FAILED, CANCELED;
	}

	private void setDefaultValues()
	{
		setSubmissionDate(new Date());
		setStatus(Status.PENDING);
	}
}