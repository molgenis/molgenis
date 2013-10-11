package org.molgenis.omx.workflow;

import java.util.List;

public interface WorkflowService
{
	Workflow getWorkflow(Integer workflowId) throws WorkflowException;

	List<Workflow> getWorkflows();

	WorkflowElement getWorkflowElement(Integer workflowElementId) throws WorkflowException;
}
