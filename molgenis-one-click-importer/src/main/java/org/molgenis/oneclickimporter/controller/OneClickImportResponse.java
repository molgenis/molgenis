package org.molgenis.oneclickimporter.controller;


public class OneClickImportResponse
{
	private final String entityId;
	private final String baseFileName;

	public OneClickImportResponse(String entityId, String baseFileName)
	{
		this.entityId = entityId;
		this.baseFileName = baseFileName;
	}

	public String getEntityId()
	{
		return entityId;
	}

	public String getBaseFileName()
	{
		return baseFileName;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		OneClickImportResponse that = (OneClickImportResponse) o;

		if (!entityId.equals(that.entityId)) return false;
		return baseFileName.equals(that.baseFileName);
	}

	@Override
	public int hashCode()
	{
		int result = entityId.hashCode();
		result = 31 * result + baseFileName.hashCode();
		return result;
	}
}
