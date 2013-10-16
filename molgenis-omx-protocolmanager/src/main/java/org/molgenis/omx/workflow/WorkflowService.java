package org.molgenis.omx.workflow;

import java.util.List;

public interface WorkflowService
{
	Workflow getWorkflow(Integer workflowId) throws WorkflowException;

	List<Workflow> getWorkflows();

	WorkflowElement getWorkflowElement(Integer workflowElementId) throws WorkflowException;

	void deleteWorkflowElementDataRow(Integer rowId) throws WorkflowException;

	/**
	 * Create new data row for the given workflow element and optionally create connections with previous workflow
	 * elements.
	 * 
	 * @param workflowElementId
	 *            workflow element id (required)
	 * @param inputWorkflowElementDataRowIds
	 *            ids of rows of input workflow elements (optional)
	 */
	void createWorkflowElementDataRowWithConnections(Integer workflowElementId,
			List<Integer> inputWorkflowElementDataRowIds);

	void updateWorkflowElementDataRowValue(Integer workflowElementDataRowId, Integer featureId, String rawValue);
}
