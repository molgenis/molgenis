package org.molgenis.compute.ui.model;

import java.util.List;

import org.molgenis.compute.ui.meta.UIWorkflowProtocolMetaData;
import org.molgenis.data.support.MapEntity;

import com.google.common.collect.Lists;

public class UIWorkflowProtocol extends MapEntity
{
	private static final long serialVersionUID = 289167506331360710L;

	public UIWorkflowProtocol()
	{
		super(UIWorkflowProtocolMetaData.IDENTIFIER);
	}

	public UIWorkflowProtocol(String identifier, String name, String template)
	{
		this();
		set(UIWorkflowProtocolMetaData.IDENTIFIER, identifier);
		set(UIWorkflowProtocolMetaData.NAME, name);
		set(UIWorkflowProtocolMetaData.TEMPLATE, template);
	}

	public String getIdentifier()
	{
		return getString(UIWorkflowProtocolMetaData.IDENTIFIER);
	}

	public String getName()
	{
		return getString(UIWorkflowProtocolMetaData.NAME);
	}

	public void setName(String name)
	{
		set(UIWorkflowProtocolMetaData.NAME, name);
	}

	public String getTemplate()
	{
		return getString(UIWorkflowProtocolMetaData.TEMPLATE);
	}

	public void setTemplate(String template)
	{
		set(UIWorkflowProtocolMetaData.TEMPLATE, template);
	}

	public List<UIParameter> getParameters()
	{
		Iterable<UIParameter> parameters = getEntities(UIWorkflowProtocolMetaData.PARAMETERS, UIParameter.class);
		if (parameters == null) return Lists.newArrayList();
		return Lists.newArrayList(parameters);
	}

	public void setParameters(List<UIParameter> parameters)
	{
		set(UIWorkflowProtocolMetaData.PARAMETERS, parameters);
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

		return getIdentifier().equals(((UIWorkflowProtocol) obj).getIdentifier());
	}
}
