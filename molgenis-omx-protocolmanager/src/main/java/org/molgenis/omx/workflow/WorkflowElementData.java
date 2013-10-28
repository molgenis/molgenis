package org.molgenis.omx.workflow;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class WorkflowElementData
{
	private final List<WorkflowElementDataRow> elementDataRows;

	public WorkflowElementData(List<WorkflowElementDataRow> elementDataRows)
	{
		if (elementDataRows == null) throw new IllegalArgumentException("Element data rows is null");
		this.elementDataRows = elementDataRows;
	}

	public List<WorkflowElementDataRow> getElementDataRows()
	{
		return elementDataRows;
	}

	public List<WorkflowElementDataRow> getElementDataRows(Integer elementDataRowConnectionSourceId)
	{
		final Integer sourceId = elementDataRowConnectionSourceId;
		return Lists.newArrayList(Collections2.filter(elementDataRows, new Predicate<WorkflowElementDataRow>()
		{

			@Override
			public boolean apply(WorkflowElementDataRow elementDataRow)
			{
				List<WorkflowElementDataRowConnection> elementDataRowConnections = elementDataRow
						.getIncomingElementDataRowConnections();
				if (elementDataRowConnections != null)
				{
					for (WorkflowElementDataRowConnection elementDataRowConnection : elementDataRowConnections)
					{
						if (elementDataRowConnection.getInputDataRow().getId() == sourceId) return true;
					}
				}
				return false;
			}
		}));
	}
}
