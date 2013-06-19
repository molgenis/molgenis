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
	private final String dataSetIdentifier;

	public DataSetImportedEvent(Object source, String dataSetIdentifier)
	{
		super(source);
		this.dataSetIdentifier = dataSetIdentifier;
	}

	public String getDataSetIdentifier()
	{
		return dataSetIdentifier;
	}

}
