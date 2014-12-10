package org.molgenis.compute.ui.workflow;

import org.apache.log4j.Logger;
import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.compute.ui.model.UIWorkflow;
import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkflowManageServiceImpl implements WorkflowManageService
{
	private static final Logger logger = Logger.getLogger(WorkflowManageServiceImpl.class);

	private final DataService dataService;

	@Autowired
	public WorkflowManageServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	@Override
	public boolean allowAction(String actionId, String entityName)
	{
		logger.debug("checking if data explorer action [" + actionId + "] must be enabled for entity [" + entityName
				+ "]");
		UIWorkflow uiWorkflow = dataService.findOne(UIWorkflowMetaData.INSTANCE.getName(), actionId, UIWorkflow.class);
		if (uiWorkflow == null) return false;

		String targetType = uiWorkflow.getTargetType();
		return entityName != null && targetType != null && entityName.equals(targetType);
	}
}
