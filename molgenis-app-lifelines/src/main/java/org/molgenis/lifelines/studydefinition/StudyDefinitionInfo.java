package org.molgenis.lifelines.studydefinition;

public class StudyDefinitionInfo
{
	private final String id;
	private final String name;

	public StudyDefinitionInfo(String id, String name)
	{
		if (id == null) throw new IllegalArgumentException("Id is null");
		this.id = id;
		this.name = name;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

}
