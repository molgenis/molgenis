package org.molgenis.omx.workflow;

import java.util.List;

public interface WorkflowService
{
	Workflow getWorkflow(Integer workflowId) throws WorkflowException;

	List<Workflow> getWorkflows();

	WorkflowElement getWorkflowElement(Integer workflowElementId) throws WorkflowException;

	void deleteWorkflowElementDataRow(Integer rowId) throws WorkflowException;

	// FIXME continue working on feature
	// void createWorkflowElementDataRowConnections()
}
