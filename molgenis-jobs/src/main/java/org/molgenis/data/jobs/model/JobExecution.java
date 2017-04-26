package org.molgenis.data.jobs.model;

import org.molgenis.auth.User;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.springframework.util.StringUtils;

import java.time.Instant;

import static org.molgenis.data.jobs.model.JobExecutionMetaData.*;

/**
 * Superclass that represents a job execution.
 * <p>
 * Do not add abstract identifier to this class, see EntitySerializerTest
 */
public class JobExecution extends StaticEntity
{
	public JobExecution(Entity entity)
	{
		super(entity);
	}

	public JobExecution(EntityType entityType)
	{
		super(entityType);
		setDefaultValues();

	}

	public JobExecution(String identifier, EntityType entityType)
	{
		super(entityType);
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

	public void setUser(User value)
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

	public Instant getSubmissionDate()
	{
		return getInstant(SUBMISSION_DATE);
	}

	public void setSubmissionDate(Instant value)
	{
		set(SUBMISSION_DATE, value);
	}

	public Instant getStartDate()
	{
		return getInstant(START_DATE);
	}

	public void setStartDate(Instant value)
	{
		set(START_DATE, value);
	}

	public Instant getEndDate()
	{
		return getInstant(END_DATE);
	}

	public void setEndDate(Instant value)
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
		return getString(RESULT_URL);
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
		PENDING, RUNNING, SUCCESS, FAILED, CANCELED
	}

	private void setDefaultValues()
	{
		setSubmissionDate(Instant.now());
		setStatus(Status.PENDING);
	}
}