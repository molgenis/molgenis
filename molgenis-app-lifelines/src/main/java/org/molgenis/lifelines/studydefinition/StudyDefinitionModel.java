package org.molgenis.lifelines.studydefinition;

public class StudyDefinitionModel
{
	private final String id;
	private final boolean loaded;

	public StudyDefinitionModel(String id, boolean loaded)
	{
		this.id = id;
		this.loaded = loaded;
	}

	public String getId()
	{
		return id;
	}

	public boolean isLoaded()
	{
		return loaded;
	}

}
