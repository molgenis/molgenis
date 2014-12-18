package org.molgenis.compute.ui.workflow.event;

import static org.molgenis.dataexplorer.event.DataExplorerRegisterEvent.Type.DEREGISTER;
import static org.molgenis.dataexplorer.event.DataExplorerRegisterEvent.Type.REGISTER;

import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.compute.ui.model.UIWorkflow;
import org.molgenis.compute.ui.workflow.WorkflowManageService;
import org.molgenis.data.DataService;
import org.molgenis.dataexplorer.event.DataExplorerRegisterActionEvent;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

/**
 * Register and deregister workflow action handlers with the data explorer
 */
@Service
public class WorkflowHandlerRegistratorServiceImpl implements WorkflowHandlerRegistratorService,
		ApplicationEventPublisherAware
{
	private final WorkflowManageService workflowManageService;
	private final DataService dataService;
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public WorkflowHandlerRegistratorServiceImpl(DataService dataService, WorkflowManageService workflowManageService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.workflowManageService = workflowManageService;
		this.dataService = dataService;
	}

	@Override
	@RunAsSystem
	public void registerWorkflowHandlers()
	{
		Iterable<UIWorkflow> uiWorkflows = dataService.findAll(UIWorkflowMetaData.INSTANCE.getName(), UIWorkflow.class);
		for (UIWorkflow uiWorkflow : uiWorkflows)
		{
			registerWorkflowHandler(uiWorkflow);
		}
	}

	@Override
	@RunAsSystem
	public void registerWorkflowHandler(UIWorkflow uiWorkflow)
	{
		String uiWorkflowId = uiWorkflow.getIdValue().toString();
		String uiWorkflowName = uiWorkflow.getName();

		applicationEventPublisher.publishEvent(new DataExplorerRegisterActionEvent(REGISTER, workflowManageService,
				uiWorkflowId, uiWorkflowName));
	}

	@Override
	public void deregisterWorkflowHandler(UIWorkflow uiWorkflow)
	{
		String uiWorkflowId = uiWorkflow.getIdValue().toString();
		String uiWorkflowName = uiWorkflow.getName();
		applicationEventPublisher.publishEvent(new DataExplorerRegisterActionEvent(DEREGISTER, workflowManageService,
				uiWorkflowId, uiWorkflowName));
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher)
	{
		this.applicationEventPublisher = publisher;
	}
}
