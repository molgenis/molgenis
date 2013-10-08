package org.molgenis.omx.workflow;

import java.util.List;

public interface WorkflowService
{
	Workflow getWorkflow(Integer workflowId) throws WorkflowException;

	List<Workflow> getWorkflows();

	WorkflowStep getWorkflowStep(Integer workflowStepId) throws WorkflowException;

	WorkflowStepData getWorkflowStepData(Integer workflowStepId) throws WorkflowException;
}
