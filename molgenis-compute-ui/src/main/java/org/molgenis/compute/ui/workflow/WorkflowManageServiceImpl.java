package org.molgenis.compute.ui.workflow;

import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.compute.ui.ComputeUiException;
import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.compute.ui.model.UIWorkflow;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

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

	@Override
	public String performAction(String actionId, String entityName, List<QueryRule> queryRules)
	{
		// TODO how to get analysis URL?
		Escaper escaper = UrlEscapers.urlPathSegmentEscaper();
		return "/menu/main/analysis?workflow=" + escaper.escape(actionId) + "&target=" + escaper.escape(entityName)
				+ "&q=" + escaper.escape(queryRules.toString());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU')")
	public void updateWorkflow(String identifier, String name, String description, String targetFullName, boolean active)
	{
		UIWorkflow workflow = dataService.findOne(UIWorkflowMetaData.INSTANCE.getName(), identifier, UIWorkflow.class);
		if (workflow == null) throw new ComputeUiException("Unknown workflow '" + identifier + "'");

		// Check if target EntityMetaData exists
		if (!dataService.hasRepository(targetFullName)) throw new ComputeUiException("Unknown target entity '"
				+ targetFullName + "'");

		// If name is updated check if it not already exists
		if (!workflow.getName().equals(name))
		{
			Entity existing = dataService.findOne(UIWorkflowMetaData.INSTANCE.getName(),
					new QueryImpl().eq(UIWorkflowMetaData.NAME, name));
			if (existing != null) throw new ComputeUiException("There is already a workflow named '" + name + "'");
		}

		workflow.setName(name);
		workflow.setDescription(description);
		workflow.setTargetType(targetFullName);
		workflow.setActive(active);
		dataService.update(UIWorkflowMetaData.INSTANCE.getName(), workflow);
	}
}
