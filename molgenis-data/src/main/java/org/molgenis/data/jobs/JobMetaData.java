package org.molgenis.data.jobs;

import java.util.Date;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;

public class JobMetaData extends DefaultEntity
{
	private static final long serialVersionUID = -4064249548140446038L;

	public static final String ENTITY_NAME = "JobMetaData";
	public static final String IDENTIFIER = "identifier"; // Job ID
	public static final String USER = "user"; // Owner of the job
	public static final String STATUS = "status"; // Job status like running or failed
	public static final String TYPE = "type"; // Job type like ImportJob
	public static final String SUBMISSION_DATE = "submissionDate";
	public static final String START_DATE = "startDate";
	public static final String END_DATE = "endDate";
	public static final String PROGRESS_INT = "progressInt"; // Number of processed entities
	public static final String PROGRESS_MESSAGE = "progressMessage";
	public static final String PROGRESS_MAX = "progressMax"; // Max number of entities to process

	public static enum Status
	{
		PENDING, RUNNING, SUCCESS, FAILED, CANCELED
	}

	public JobMetaData(DataService dataService)
	{
		super(new JobMetaDataMetaData(), dataService);
	}

	public JobMetaData(DataService dataService, EntityMetaData emd)
	{
		super(emd, dataService);
	}

	public String getIdentifier()
	{
		return getString(IDENTIFIER);
	}

	public void setIdentifier(String value)
	{
		set(IDENTIFIER, value);
	}

	public MolgenisUser getUser()
	{
		return getEntity(USER, MolgenisUser.class);
	}

	public void setUser(MolgenisUser value)
	{
		set(USER, value);
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
}
