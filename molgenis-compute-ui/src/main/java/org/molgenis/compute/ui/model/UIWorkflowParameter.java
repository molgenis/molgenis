package org.molgenis.compute.ui.model;

import org.molgenis.compute.ui.meta.UIWorkflowParameterMetaData;
import org.molgenis.data.support.MapEntity;

public class UIWorkflowParameter extends MapEntity
{
	private static final long serialVersionUID = 3934581781371762696L;

	public UIWorkflowParameter()
	{
		super(UIWorkflowParameterMetaData.INSTANCE);
	}

	public UIWorkflowParameter(String identifier, String key, String value)
	{
		this();
		set(UIWorkflowParameterMetaData.IDENTIFIER, identifier);
		setKey(key);
		setValue(value);
	}

	public String getIdentifier()
	{
		return getString(UIWorkflowParameterMetaData.IDENTIFIER);
	}

	public String getKey()
	{
		return getString(UIWorkflowParameterMetaData.KEY);
	}

	public void setKey(String key)
	{
		set(UIWorkflowParameterMetaData.KEY, key);
	}

	public String getValue()
	{
		return getString(UIWorkflowParameterMetaData.VALUE);
	}

	public void setValue(String value)
	{
		set(UIWorkflowParameterMetaData.VALUE, value);
	}

}
