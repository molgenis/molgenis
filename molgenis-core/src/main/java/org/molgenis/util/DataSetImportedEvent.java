package org.molgenis.util;

import org.springframework.context.ApplicationEvent;

/**
 * Sprinf application event that is published when a dataset is imported with the DataSetImporter
 * 
 * @author erwin
 * 
 */
public class DataSetImportedEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 1L;
	private final Integer dataSetId;

	public DataSetImportedEvent(Object source, Integer dataSetId)
	{
		super(source);
		this.dataSetId = dataSetId;
	}

	public Integer getDataSetId()
	{
		return dataSetId;
	}

}
