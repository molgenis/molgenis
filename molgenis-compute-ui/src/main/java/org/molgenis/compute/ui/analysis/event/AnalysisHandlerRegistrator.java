package org.molgenis.compute.ui.analysis.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class AnalysisHandlerRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final AnalysisHandlerRegistratorService analysisHandlerRegistratorService;

	@Autowired
	public AnalysisHandlerRegistrator(AnalysisHandlerRegistratorService analysisHandlerRegistratorService)
	{
		if (analysisHandlerRegistratorService == null)
		{
			throw new IllegalArgumentException("analysisHandlerRegistratorService is null");
		}
		this.analysisHandlerRegistratorService = analysisHandlerRegistratorService;
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		analysisHandlerRegistratorService.registerAnalysisHandlers();
	}
}
