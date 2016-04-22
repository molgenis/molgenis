package org.molgenis.gavin.job;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.jobs.JobExecution;

import static org.molgenis.gavin.job.GavinJobExecutionMetaData.FILENAME;

public class GavinJobExecution extends JobExecution
{
	private static final long serialVersionUID = 1L;
	public static final String GAVIN = "gavin";

	public static final EntityMetaData META_DATA = new GavinJobExecutionMetaData();

	public GavinJobExecution(DataService dataService)
	{
		super(dataService, META_DATA);
		setType(GAVIN);
	}

	public String getFilename()
	{
		return getString(FILENAME);
	}

	public void setFilename(String fileName)
	{
		set(FILENAME, fileName);
	}
}
