package org.molgenis.compute5.model;

import org.molgenis.compute5.Validator;

/** Input for a protocol.*/
public class Input
{
	//unique name within a protocol
	private String name;
	//description of this parameter
	private String description;
	private String type;

	public Input(String name)
	{
		this.setName(name);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		//Validate that 'name' does only contain a-zA-Z0-9
		Validator.validateParameterName(name);
		if (Protocol.reservedNames.contains(name)) throw new RuntimeException("input name cannot be '" + name
				+ "' because it is a reserved word. The parameters in the compute.properties that are set by Molgenis Compute," +
				" are automatically available in your scripts as e.g. $rundir.");
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

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
}
