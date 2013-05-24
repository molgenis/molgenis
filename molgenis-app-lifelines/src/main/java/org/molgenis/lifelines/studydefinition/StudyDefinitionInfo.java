package org.molgenis.lifelines.studydefinition;

public class StudyDefinitionInfo
{
	private final String id;

	public StudyDefinitionInfo(String id)
	{
		if (id == null) throw new IllegalArgumentException("Id is null");
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

}
