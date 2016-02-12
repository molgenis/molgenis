package org.molgenis.data.annotation.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.jobs.JobMetaData;

public class AnnotationJobMetaData extends JobMetaData
{
	private static final long serialVersionUID = -4064249548140446038L;

	public static final String ENTITY_NAME = "AnnotationJobMetaData";
	public static final String TARGET = "target";
	public static final String LOG = "log";
	public static final String ANNOTATORS = "annotators";

	public static final EntityMetaData META_DATA = new AnnotationJobMetaDataMetaData();

	public AnnotationJobMetaData(DataService dataService)
	{
		super(dataService, META_DATA);
	}

	public String getTarget()
	{
		return getString(TARGET);
	}

	public void setTarget(String value)
	{
		set(TARGET, value);
	}

	public String getLog()
	{
		return getString(LOG);
	}

	public void setLog(String value)
	{
		set(LOG, value);
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
