package org.molgenis.data.mapper.mapping.model;

import java.util.Collections;
import java.util.List;

import org.molgenis.data.AttributeMetaData;

/**
 * Created by charbonb on 14/01/15.
 */
public class AttributeMapping
{
	private String identifier;
	private final AttributeMetaData targetAttributeMetaData;
	private final List<AttributeMetaData> sourceAttributeMetaDatas;
	private String algorithm;

	public AttributeMapping(String identifier, AttributeMetaData targetAttributeMetaData, String algorithm,
			List<AttributeMetaData> sourceAttributeMetaDatas)
	{
		this.identifier = identifier;
		this.targetAttributeMetaData = targetAttributeMetaData;
		this.sourceAttributeMetaDatas = sourceAttributeMetaDatas;
		this.algorithm = algorithm;

	}

	/**
	 * Creates a new empty AttributeMapping
	 * 
	 * @param targetAttributeMetaData2
	 */
	public AttributeMapping(AttributeMetaData target)
	{
		this.identifier = null;
		this.targetAttributeMetaData = target;
		this.sourceAttributeMetaDatas = Collections.emptyList();
		this.algorithm = null;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public AttributeMetaData getTargetAttributeMetaData()
	{
		return targetAttributeMetaData;
	}

	public List<AttributeMetaData> getSourceAttributeMetaDatas()
	{
		return sourceAttributeMetaDatas;
	}

	public String getAlgorithm()
	{
		return algorithm;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((algorithm == null) ? 0 : algorithm.hashCode());
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((targetAttributeMetaData == null) ? 0 : targetAttributeMetaData.hashCode());
		result = prime * result + ((sourceAttributeMetaDatas == null) ? 0 : sourceAttributeMetaDatas.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AttributeMapping other = (AttributeMapping) obj;
		if (algorithm == null)
		{
			if (other.algorithm != null) return false;
		}
		else if (!algorithm.equals(other.algorithm)) return false;
		if (identifier == null)
		{
			if (other.identifier != null) return false;
		}
		else if (!identifier.equals(other.identifier)) return false;
		if (targetAttributeMetaData == null)
		{
			if (other.targetAttributeMetaData != null) return false;
		}
		else if (!targetAttributeMetaData.equals(other.targetAttributeMetaData)) return false;
		if (sourceAttributeMetaDatas == null)
		{
			if (other.sourceAttributeMetaDatas != null) return false;
		}
		else if (!sourceAttributeMetaDatas.equals(other.sourceAttributeMetaDatas)) return false;
		return true;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	@Override
	public String toString()
	{
		return "AttributeMapping [identifier=" + identifier + ", targetAttributeMetaData=" + targetAttributeMetaData
				+ ", sourceAttributeMetaDatas=" + sourceAttributeMetaDatas + ", algorithm=" + algorithm + "]";
	}

	public void setAlgorithm(String algorithm)
	{
		this.algorithm = algorithm;
	}

}
