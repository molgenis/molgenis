package org.molgenis.omx.workflow;

import org.molgenis.data.DataService;

public class WorkflowElementDataRowConnection
{
	private final Integer id;
	private final WorkflowElementDataRow inputDataRow;
	private final WorkflowElementDataRow outputDataRow;

	public WorkflowElementDataRowConnection(ObservationSetFlow observationSetFlow, DataService dataService)
	{
		if (observationSetFlow == null) throw new IllegalArgumentException("ObservationSetFlow is null");
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.id = observationSetFlow.getId();
		this.inputDataRow = new WorkflowElementDataRow(observationSetFlow.getSource(), dataService);
		this.outputDataRow = new WorkflowElementDataRow(observationSetFlow.getDestination(), dataService);
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
