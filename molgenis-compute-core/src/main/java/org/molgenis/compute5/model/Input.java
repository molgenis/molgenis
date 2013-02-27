package org.molgenis.compute5.model;

/** Input for a protocol.*/
public class Input
{
	//unique name within a protocol
	String name;
	
	//description of this parameter
	String description;
	
	//FIXME use molgenis FieldType framework
	//type of this input, e.g. string or list (influences hasOne folding)
	String type;

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
		//FIXME add validation to ensure 'name' does only contain a-zA-Z0-9
		if (Protocol.reservedNames.contains(name)) throw new RuntimeException("input name cannot be '" + name
				+ "' because it is a reserved word");
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
