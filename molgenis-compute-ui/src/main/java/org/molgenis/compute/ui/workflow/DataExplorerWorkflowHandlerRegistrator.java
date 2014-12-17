package org.molgenis.compute.ui.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class DataExplorerWorkflowHandlerRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{

	private final WorkflowHandlerRegistratorService dataExplorerWorkflowHandlerRegistratorService;

	@Autowired
	public DataExplorerWorkflowHandlerRegistrator(
			WorkflowHandlerRegistratorService dataExplorerWorkflowHandlerRegistratorService)
	{
		if (dataExplorerWorkflowHandlerRegistratorService == null) throw new IllegalArgumentException(
				"dataExplorerWorkflowHandlerRegistratorService is null");
		this.dataExplorerWorkflowHandlerRegistratorService = dataExplorerWorkflowHandlerRegistratorService;
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		dataExplorerWorkflowHandlerRegistratorService.registerWorkflowHandlers();
	}
}
