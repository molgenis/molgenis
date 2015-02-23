package org.molgenis.compute.ui.model;

import com.google.common.collect.Lists;
import org.molgenis.compute.ui.meta.UIParameterMetaData;
import org.molgenis.compute.ui.meta.UIWorkflowParameterMetaData;
import org.molgenis.data.support.MapEntity;

import java.util.List;

public class UIWorkflowParameter extends MapEntity
{
	private static final long serialVersionUID = 3934581781371762696L;

	public UIWorkflowParameter()
	{
		super(UIWorkflowParameterMetaData.INSTANCE);
	}

	public UIWorkflowParameter(String identifier, String key, List<UIWorkflowParameterValue> values)
	{
		this();
		set(UIWorkflowParameterMetaData.IDENTIFIER, identifier);
		setKey(key);
		setValue(values);
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

	public List<UIWorkflowParameterValue> getValues()
	{
		Iterable<UIWorkflowParameterValue> values = getEntities(UIWorkflowParameterMetaData.VALUES,
				UIWorkflowParameterValue.class);
		if (values == null) return Lists.newArrayList();
		return Lists.newArrayList(values);
	}

	public void setValue(List<UIWorkflowParameterValue> value)
	{
		set(UIWorkflowParameterMetaData.VALUES, value);
	}

}
