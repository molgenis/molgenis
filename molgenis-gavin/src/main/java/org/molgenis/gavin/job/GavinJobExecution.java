package org.molgenis.gavin.job;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.jobs.JobExecution;

public class GavinJobExecution extends JobExecution
{
	private static final long serialVersionUID = 1L;
	public static final String GAVIN = "gavin";
	public static final String ENTITY_NAME = "GavinJobExecution";

	public static final EntityMetaData META_DATA = new GavinJobExecutionMetaData();

	public GavinJobExecution(DataService dataService)
	{
		super(dataService, META_DATA);
		setType(GAVIN);
	}
}
