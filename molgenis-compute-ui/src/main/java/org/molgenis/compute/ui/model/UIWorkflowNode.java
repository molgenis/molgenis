package org.molgenis.compute.ui.model;

import java.util.List;

import org.molgenis.compute.ui.meta.UIWorkflowNodeMetaData;
import org.molgenis.data.support.MapEntity;

import com.google.common.collect.Lists;

public class UIWorkflowNode extends MapEntity
{
	private static final long serialVersionUID = 6414697555252864326L;

	public UIWorkflowNode()
	{
		super(UIWorkflowNodeMetaData.IDENTIFIER);
	}

	public UIWorkflowNode(String identifier, String name, UIWorkflowProtocol protocol)
	{
		this();
		set(UIWorkflowNodeMetaData.IDENTIFIER, identifier);
		set(UIWorkflowNodeMetaData.NAME, name);
		set(UIWorkflowNodeMetaData.PROTOCOL, protocol);
	}

	public String getIdentifier()
	{
		return getString(UIWorkflowNodeMetaData.IDENTIFIER);
	}

	public String getName()
	{
		return getString(UIWorkflowNodeMetaData.NAME);
	}

	public void setName(String name)
	{
		set(UIWorkflowNodeMetaData.NAME, name);
	}

	public UIWorkflowProtocol getProtocol()
	{
		return getEntity(UIWorkflowNodeMetaData.PROTOCOL, UIWorkflowProtocol.class);
	}

	public void setProtocol(UIWorkflowProtocol protocol)
	{
		set(UIWorkflowNodeMetaData.PROTOCOL, protocol);
	}

	public List<UIWorkflowNode> getPreviousNodes()
	{
		Iterable<UIWorkflowNode> prevNodes = getEntities(UIWorkflowNodeMetaData.PREVIOUS_NODES, UIWorkflowNode.class);
		if (prevNodes == null) return Lists.newArrayList();
		return Lists.newArrayList(prevNodes);
	}

	public void addPreviousNode(UIWorkflowNode node)
	{
		List<UIWorkflowNode> prevNodes = getPreviousNodes();
		prevNodes.add(node);
		set(UIWorkflowNodeMetaData.PREVIOUS_NODES, prevNodes);
	}

	public List<UIParameterMapping> getParameterMappings()
	{
		Iterable<UIParameterMapping> mappings = getEntities(UIWorkflowNodeMetaData.PARAMETER_MAPPINGS,
				UIParameterMapping.class);
		if (mappings == null) return Lists.newArrayList();
		return Lists.newArrayList(mappings);
	}

	public void setParameterMappings(List<UIParameterMapping> mappings)
	{
		set(UIWorkflowNodeMetaData.PARAMETER_MAPPINGS, mappings);
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

		return getIdentifier().equals(((UIWorkflowNode) obj).getIdentifier());
	}
}
