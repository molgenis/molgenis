package org.molgenis.compute.ui.workflow;

import org.molgenis.compute.ui.model.UIWorkflow;

public interface DataExplorerWorkflowHandlerRegistratorService
{

	void registerWorkflowHandlers();

	void registerWorkflowHandler(UIWorkflow uiWorkflow);

}
