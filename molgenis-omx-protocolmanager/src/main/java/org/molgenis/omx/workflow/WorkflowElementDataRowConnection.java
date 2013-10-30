package org.molgenis.omx.workflow;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;

public class WorkflowElementDataRowConnection
{
	private final Integer id;
	private final WorkflowElementDataRow inputDataRow;
	private final WorkflowElementDataRow outputDataRow;

	public WorkflowElementDataRowConnection(ObservationSetFlow observationSetFlow, Database database)
			throws DatabaseException
	{
		if (observationSetFlow == null) throw new IllegalArgumentException("ObservationSetFlow is null");
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.id = observationSetFlow.getId();
		this.inputDataRow = new WorkflowElementDataRow(observationSetFlow.getSource(), database);
		this.outputDataRow = new WorkflowElementDataRow(observationSetFlow.getDestination(), database);
	}

	public Integer getId()
	{
		return id;
	}

	public WorkflowElementDataRow getInputDataRow()
	{
		return inputDataRow;
	}

	public WorkflowElementDataRow getOutputDataRow()
	{
		return outputDataRow;
	}
}
