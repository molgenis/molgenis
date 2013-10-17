package org.molgenis.omx.workflow;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;

public class WorkflowFeature
{
	private final Integer id;
	private final String name;
	private final String dataType;
	private final Boolean required;

	public WorkflowFeature(ObservableFeature feature, Protocol protocol)
	{
		if (feature == null) throw new IllegalArgumentException("ObservableFeature is null");
		this.id = feature.getId();
		this.name = feature.getName();
		this.dataType = feature.getDataType();
		this.required = protocol.getRequiredFeatures().contains(feature);
	}

	public Integer getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getDataType()
	{
		return dataType;
	}

	public Boolean getRequired()
	{
		return required;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		WorkflowFeature other = (WorkflowFeature) obj;
		if (id == null)
		{
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
		return true;
	}
}
