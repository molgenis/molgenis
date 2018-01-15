package org.molgenis.jobs.model;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.support.StaticEntity;

import javax.annotation.Nullable;
import java.time.Instant;

import static org.apache.commons.lang3.StringUtils.*;
import static org.molgenis.jobs.model.JobExecutionMetaData.*;

/**
 * Superclass that represents a job execution.
 * <p>
 * Do not add abstract identifier to this class, see EntitySerializerTest
 */
public class JobExecution extends StaticEntity
{
	public static final String TRUNCATION_BANNER = "<<< THIS LOG HAS BEEN TRUNCATED >>>";
	/**
	 * If the progress message is larger than this value, it will be abbreviated.
	 * This value shouldn't exceed the max length of the {@link org.molgenis.data.meta.AttributeType#STRING}
	 */
	public static final int MAX_PROGRESS_MESSAGE_LENGTH = 255;
	/**
	 * If log is larger than this value, it will be truncated.
	 * This value shouldn't exceed the max length of the {@link org.molgenis.data.meta.AttributeType#TEXT}
	 */
	public static final int MAX_LOG_LENGTH = 65535;
	private boolean logTruncated = false;

	public JobExecution(Entity entity)
	{
		super(entity);
	}

	public JobExecution(EntityType entityType)
	{
		super(entityType);
	}

	public JobExecution(String identifier, EntityType entityType)
	{
		super(entityType);
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

	@Nullable
	public Instant getStartDate()
	{
		return getInstant(START_DATE);
	}

	public void setStartDate(Instant value)
	{
		set(START_DATE, value);
	}

	@Nullable
	public Instant getEndDate()
	{
		return getInstant(END_DATE);
	}

	public void setEndDate(Instant value)
	{
		set(END_DATE, value);
	}

	@Nullable
	public Integer getProgressInt()
	{
		return getInt(PROGRESS_INT);
	}

	public void setProgressInt(Integer value)
	{
		set(PROGRESS_INT, value);
	}

	@Nullable
	public String getProgressMessage()
	{
		return getString(PROGRESS_MESSAGE);
	}

	public void setProgressMessage(String value)
	{
		set(PROGRESS_MESSAGE, StringUtils.abbreviate(value, MAX_PROGRESS_MESSAGE_LENGTH));
	}

	@Nullable
	public Integer getProgressMax()
	{
		return getInt(PROGRESS_MAX);
	}

	public void setProgressMax(Integer value)
	{
		set(PROGRESS_MAX, value);
	}

	@Nullable
	public String getLog()
	{
		return getString(LOG);
	}

	private void setLog(String value)
	{
		set(LOG, value);
	}

	@Nullable
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
		if (isEmpty(email))
		{
			return new String[] {};
		}
		return email.split(",");
	}

	public String[] getFailureEmail()
	{
		String email = getString(FAILURE_EMAIL);
		if (isEmpty(email))
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

	public void setScheduledJobId(String scheduledJobId)
	{
		set(SCHEDULED_JOB_ID, scheduledJobId);
	}

	@Nullable
	public String getScheduledJobId()
	{
		return getString(SCHEDULED_JOB_ID);
	}

	/**
	 * Appends a log message to the execution log.
	 * The first time the log exceeds MAX_LOG_LENGTH, it gets truncated and the TRUNCATION_BANNER gets added.
	 * Subsequent calls to appendLog will be ignored.
	 *
	 * @param formattedMessage The formatted message to append to the log.
	 */
	void appendLog(String formattedMessage)
	{
		if (logTruncated) return;
		String combined = join(getLog(), formattedMessage);
		if (combined.length() > MAX_LOG_LENGTH)
		{
			String truncated = abbreviate(combined, MAX_LOG_LENGTH - TRUNCATION_BANNER.length() * 2 - 2);
			combined = join(new String[] { TRUNCATION_BANNER, truncated, TRUNCATION_BANNER }, "\n");
			logTruncated = true;
		}
		setLog(combined);
	}

	public enum Status
	{
		PENDING, RUNNING, SUCCESS, FAILED, CANCELED
	}
}