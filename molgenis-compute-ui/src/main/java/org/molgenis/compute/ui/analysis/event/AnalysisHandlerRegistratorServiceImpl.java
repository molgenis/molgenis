package org.molgenis.compute.ui.analysis.event;

import static org.molgenis.dataexplorer.event.DataExplorerRegisterEvent.Type.REGISTER;

import org.molgenis.compute.ui.analysis.AnalysisPluginController;
import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.compute.ui.model.UIWorkflow;
import org.molgenis.compute.ui.model.decorator.UIWorkflowDecorator;
import org.molgenis.data.DataService;
import org.molgenis.dataexplorer.event.DataExplorerRegisterRefCellClickEvent;
import org.molgenis.dataexplorer.event.DataExplorerRegisterRefCellClickEventHandler;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

/**
 * Register and deregister workflow action handlers with the data explorer
 */
@Service
public class AnalysisHandlerRegistratorServiceImpl implements AnalysisHandlerRegistratorService,
		ApplicationEventPublisherAware
{
	private final DataService dataService;
	private final DataExplorerRegisterRefCellClickEventHandler dataExplorerRegisterRefCellClickEventHandler;
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public AnalysisHandlerRegistratorServiceImpl(DataService dataService,
			AnalysisPluginController analysisPluginController)
	{
		if (analysisPluginController == null) throw new IllegalArgumentException("analysisPluginController is null");
		this.dataService = dataService;
		this.dataExplorerRegisterRefCellClickEventHandler = analysisPluginController;
	}

	@Override
	@RunAsSystem
	public void registerAnalysisHandlers()
	{
		Iterable<UIWorkflow> uiWorkflows = dataService.findAll(UIWorkflowMetaData.INSTANCE.getName(), UIWorkflow.class);
		for (UIWorkflow uiWorkflow : uiWorkflows)
		{
			registerAnalysisHandler(uiWorkflow.getTargetType(), UIWorkflowDecorator.ANALYSIS_ATTRIBUTE.getName());
		}
	}

	@Override
	@RunAsSystem
	public void registerAnalysisHandler(String entityName, String attributeName)
	{
		applicationEventPublisher.publishEvent(new DataExplorerRegisterRefCellClickEvent(REGISTER, entityName,
				attributeName, dataExplorerRegisterRefCellClickEventHandler));
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher)
	{
		this.applicationEventPublisher = publisher;
	}
}
