package org.molgenis.compute.ui.workflow.event;

import org.molgenis.compute.ui.model.UIWorkflow;

public interface WorkflowHandlerRegistratorService
{
	void registerWorkflowHandlers();

	void registerWorkflowHandler(UIWorkflow uiWorkflow);

	void deregisterWorkflowHandler(UIWorkflow uiWorkflow);
}
