package org.molgenis.compute.ui.workflow;

import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.compute.ui.model.UIWorkflow;
import org.molgenis.data.DataService;
import org.molgenis.dataexplorer.controller.RegisterDataExplorerActionEvent;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

@Service
public class DataExplorerWorkflowHandlerRegistratorServiceImpl implements
		DataExplorerWorkflowHandlerRegistratorService, ApplicationEventPublisherAware
{
	private final WorkflowManageService workflowManageService;
	private final DataService dataService;
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public DataExplorerWorkflowHandlerRegistratorServiceImpl(DataService dataService,
			WorkflowManageService workflowManageService)
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
			String uiWorkflowId = uiWorkflow.getIdValue().toString();
			String uiWorkflowName = uiWorkflow.getName();
			applicationEventPublisher.publishEvent(new RegisterDataExplorerActionEvent(workflowManageService,
					uiWorkflowId, uiWorkflowName));
		}
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher)
	{
		this.applicationEventPublisher = publisher;
	}
}
