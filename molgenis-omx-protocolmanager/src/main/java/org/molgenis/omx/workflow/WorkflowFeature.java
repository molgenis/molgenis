package org.molgenis.omx.workflow;

import org.molgenis.omx.observ.ObservableFeature;

public class WorkflowFeature
{
	private final Integer id;
	private final String name;

	public WorkflowFeature(ObservableFeature feature)
	{
		if (feature == null) throw new IllegalArgumentException("ObservableFeature is null");
		this.id = feature.getId();
		this.name = feature.getName();
	}

	public Integer getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}
}
