package org.molgenis.compute.ui.workflow;

import org.hibernate.validator.constraints.NotEmpty;

public class UpdateWorkflowForm
{
	@NotEmpty
	private String name;
	private String description;

	@NotEmpty
	private String targetType;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getTargetType()
	{
		return targetType;
	}

	public void setTargetType(String targetType)
	{
		this.targetType = targetType;
	}

}
