package org.molgenis.data.jobs;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;

public class Job extends DefaultEntity
{
	private static final long serialVersionUID = -4064249548140446038L;

	public static final String ENTITY_NAME = "Job";
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

	public static final EntityMetaData META_DATA = new JobMetaData();

	public Job(DataService dataService)
	{
		super(META_DATA, dataService);
	}

	public static String getIdentifier()
	{
		return IDENTIFIER;
	}

	public static String getUser()
	{
		return USER;
	}

	public static String getStatus()
	{
		return STATUS;
	}

	public static String getType()
	{
		return TYPE;
	}

	public static String getSubmissionDate()
	{
		return SUBMISSION_DATE;
	}

	public static String getStartDate()
	{
		return START_DATE;
	}

	public static String getEndDate()
	{
		return END_DATE;
	}

	public static String getProgressInt()
	{
		return PROGRESS_INT;
	}

	public static String getProgressMessage()
	{
		return PROGRESS_MESSAGE;
	}

	public static String getProgressMax()
	{
		return PROGRESS_MAX;
	}

	public static EntityMetaData getMetaData()
	{
		return META_DATA;
	}

}
