package org.molgenis.data.annotation.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.jobs.JobExecution;

public class AnnotationJobExecution extends JobExecution
{
	private static final long serialVersionUID = -4064249548140446038L;

	public static final String ENTITY_NAME = "AnnotationJobExecution";
	public static final String TARGET_NAME = "targetName";
	public static final String ANNOTATORS = "annotators";

	public static final EntityMetaData META_DATA = new AnnotationJobExecutionMetaData();

	public AnnotationJobExecution(DataService dataService)
	{
		super(dataService, META_DATA);
		setType(ANNOTATORS);
	}

	public String getTargetName()
	{
		return getString(TARGET_NAME);
	}

	public void setTargetName(String value)
	{
		set(TARGET_NAME, value);
	}
	
	public String getAnnotators()
	{
		return getString(ANNOTATORS);
	}

	public void setAnnotators(String value)
	{
		set(ANNOTATORS, value);
	}
}
