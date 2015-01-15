package org.molgenis.compute.ui.model;

import java.util.List;

import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.data.support.MapEntity;

import com.google.common.collect.Lists;

public class UIWorkflow extends MapEntity
{
	private static final long serialVersionUID = 8684330466397478937L;
	private static boolean DEFAULT_ACTIVE = true;

	public static final String WORKFLOW_DEFAULT = "workflow.csv";
	public static final String PARAMETERS_DEFAULT = "parameters.csv";

	public UIWorkflow()
	{
		super(UIWorkflowMetaData.IDENTIFIER);
		setActive(DEFAULT_ACTIVE);
	}

	public UIWorkflow(String identifier, String name)
	{
		this();
		set(UIWorkflowMetaData.IDENTIFIER, identifier);
		set(UIWorkflowMetaData.NAME, name);
	}

	public String getIdentifier()
	{
		return getString(UIWorkflowMetaData.IDENTIFIER);
	}

	public String getName()
	{
		return getString(UIWorkflowMetaData.NAME);
	}

	public void setName(String name)
	{
		set(UIWorkflowMetaData.NAME, name);
	}

	public String getDescription()
	{
		return getString(UIWorkflowMetaData.DESCRIPTION);
	}

	public void setDescription(String description)
	{
		set(UIWorkflowMetaData.DESCRIPTION, description);
	}

	public String getGenerateScript()
	{
		return getString(UIWorkflowMetaData.GENERATE_SCRIPT);
	}

	public void setGenerateScript(String generateScript)
	{
		set(UIWorkflowMetaData.GENERATE_SCRIPT, generateScript);
	}

	public String getTargetType()
	{
		return getString(UIWorkflowMetaData.TARGET_TYPE);
	}

	public void setTargetType(String entityMetaDataName)
	{
		set(UIWorkflowMetaData.TARGET_TYPE, entityMetaDataName);
	}

	public List<UIWorkflowNode> getNodes()
	{
		Iterable<UIWorkflowNode> nodes = getEntities(UIWorkflowMetaData.NODES, UIWorkflowNode.class);
		if (nodes == null) return Lists.newArrayList();
		return Lists.newArrayList(nodes);
	}

	public void setNodes(List<UIWorkflowNode> nodes)
	{
		set(UIWorkflowMetaData.NODES, nodes);
	}

	public List<UIWorkflowParameter> getParameters()
	{
		Iterable<UIWorkflowParameter> parameters = getEntities(UIWorkflowMetaData.PARAMETERS, UIWorkflowParameter.class);
		if (parameters == null) return Lists.newArrayList();
		return Lists.newArrayList(parameters);
	}

	public void setParameters(List<UIWorkflowParameter> parameters)
	{
		set(UIWorkflowMetaData.PARAMETERS, parameters);
	}

	public boolean isActive()
	{
		return getBoolean(UIWorkflowMetaData.ACTIVE);
	}

	public void setActive(boolean active)
	{
		set(UIWorkflowMetaData.ACTIVE, active);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdentifier().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		return getIdentifier().equals(((UIWorkflow) obj).getIdentifier());
	}
}
