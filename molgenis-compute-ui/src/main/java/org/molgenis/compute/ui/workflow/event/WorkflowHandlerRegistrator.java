package org.molgenis.compute.ui.workflow.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class WorkflowHandlerRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{

	private final WorkflowHandlerRegistratorService workflowHandlerRegistratorService;

	@Autowired
	public WorkflowHandlerRegistrator(WorkflowHandlerRegistratorService workflowHandlerRegistratorService)
	{
		if (workflowHandlerRegistratorService == null)
		{
			throw new IllegalArgumentException("workflowHandlerRegistratorService is null");
		}
		this.workflowHandlerRegistratorService = workflowHandlerRegistratorService;
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		workflowHandlerRegistratorService.registerWorkflowHandlers();
	}
}
