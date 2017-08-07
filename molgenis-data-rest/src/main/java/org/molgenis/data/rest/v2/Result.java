package org.molgenis.data.rest.v2;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.List;

public class Result
{
	EntityType entityType;
	List<Attribute> matchingAttributes;
	List<Entity> matchingDatarows;
	Boolean isMatchingLabel;
	Boolean isMatchingDescription;

	public Result(EntityType entityType, List<Attribute> matchingAttributes, List<Entity> matchingDatarows,
			Boolean isMatchingLabel, Boolean isMatchingDescription)
	{
		this.entityType = entityType;
		this.matchingAttributes = matchingAttributes;
		this.matchingDatarows = matchingDatarows;
		this.isMatchingLabel = isMatchingLabel;
		this.isMatchingDescription = isMatchingDescription;
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	public void setEntityType(EntityType entityType)
	{
		this.entityType = entityType;
	}

	public List<Attribute> getMatchingAttributes()
	{
		return matchingAttributes;
	}

	public void setMatchingAttributes(List<Attribute> matchingAttributes)
	{
		this.matchingAttributes = matchingAttributes;
	}

	public List<Entity> getMatchingDatarows()
	{
		return matchingDatarows;
	}

	public void setMatchingDatarows(List<Entity> matchingDatarows)
	{
		this.matchingDatarows = matchingDatarows;
	}

	public Boolean getMatchingLabel()
	{
		return isMatchingLabel;
	}

	public void setMatchingLabel(Boolean matchingLabel)
	{
		isMatchingLabel = matchingLabel;
	}

	public Boolean getMatchingDescription()
	{
		return isMatchingDescription;
	}

	public void setMatchingDescription(Boolean matchingDescription)
	{
		isMatchingDescription = matchingDescription;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Result result = (Result) o;

		if (entityType != null ? !entityType.equals(result.entityType) : result.entityType != null) return false;
		if (matchingAttributes != null ? !matchingAttributes.equals(result.matchingAttributes) :
				result.matchingAttributes != null) return false;
		if (matchingDatarows != null ? !matchingDatarows.equals(result.matchingDatarows) :
				result.matchingDatarows != null) return false;
		if (isMatchingLabel != null ? !isMatchingLabel.equals(result.isMatchingLabel) : result.isMatchingLabel != null)
			return false;
		return isMatchingDescription != null ? isMatchingDescription.equals(result.isMatchingDescription) :
				result.isMatchingDescription == null;
	}

	@Override
	public int hashCode()
	{
		int result = entityType != null ? entityType.hashCode() : 0;
		result = 31 * result + (matchingAttributes != null ? matchingAttributes.hashCode() : 0);
		result = 31 * result + (matchingDatarows != null ? matchingDatarows.hashCode() : 0);
		result = 31 * result + (isMatchingLabel != null ? isMatchingLabel.hashCode() : 0);
		result = 31 * result + (isMatchingDescription != null ? isMatchingDescription.hashCode() : 0);
		return result;
	}

	@Override
	public String toString()
	{
		return "Result{" + "entityType=" + entityType + ", matchingAttributes=" + matchingAttributes
				+ ", matchingDatarows=" + matchingDatarows + ", isMatchingLabel=" + isMatchingLabel
				+ ", isMatchingDescription=" + isMatchingDescription + '}';
	}
}
