package org.molgenis.omx.workflow;

import java.util.List;

public class WorkflowStepData
{
	private final List<DataRow> dataMatrix;

	public WorkflowStepData(List<DataRow> dataMatrix)
	{
		if (dataMatrix == null) throw new IllegalArgumentException("Data matrix is null");
		this.dataMatrix = dataMatrix;
	}

	public List<DataRow> getDataMatrix()
	{
		return dataMatrix;
	}
}
