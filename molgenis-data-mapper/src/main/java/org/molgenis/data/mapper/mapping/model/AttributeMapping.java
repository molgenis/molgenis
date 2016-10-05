package org.molgenis.data.mapper.mapping.model;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.meta.model.AttributeMetaData;

import java.util.List;

/**
 * Created by charbonb on 14/01/15.
 */
public class AttributeMapping
{
	private String identifier;
	private final AttributeMetaData targetAttributeMetaData;
	private final List<AttributeMetaData> sourceAttributeMetaDatas;
	private String algorithm;
	private AlgorithmState algorithmState;

	public enum AlgorithmState
	{
		CURATED("CURATED"), GENERATED_HIGH("GENERATED_HIGH"), GENERATED_LOW("GENERATED_LOW"), DISCUSS("DISCUSS");

		private String label;

		AlgorithmState(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	public AttributeMapping(String identifier, AttributeMetaData targetAttributeMetaData, String algorithm,
			List<AttributeMetaData> sourceAttributeMetaDatas)
	{
		this(identifier, targetAttributeMetaData, algorithm, sourceAttributeMetaDatas, null);
	}

	public AttributeMapping(String identifier, AttributeMetaData targetAttributeMetaData, String algorithm,
			List<AttributeMetaData> sourceAttributeMetaDatas, String algorithmState)
	{
		this.identifier = identifier;
		this.targetAttributeMetaData = targetAttributeMetaData;
		this.sourceAttributeMetaDatas = sourceAttributeMetaDatas;
		this.algorithm = algorithm;
		this.algorithmState = convertToEnum(algorithmState);
	}

	/**
	 * Creates a new empty AttributeMapping
	 *
	 * @param target mapping target attribute
	 */
	public AttributeMapping(AttributeMetaData target)
	{
		this.identifier = null;
		this.targetAttributeMetaData = target;
		this.sourceAttributeMetaDatas = Lists.<AttributeMetaData>newArrayList();
		this.algorithm = null;
		this.algorithmState = null;
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

	public AlgorithmState getAlgorithmState()
	{
		return algorithmState;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((algorithm == null) ? 0 : algorithm.hashCode());
		result = prime * result + ((algorithmState == null) ? 0 : algorithmState.hashCode());
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((sourceAttributeMetaDatas == null) ? 0 : sourceAttributeMetaDatas.hashCode());
		result = prime * result + ((targetAttributeMetaData == null) ? 0 : targetAttributeMetaData.hashCode());
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
		if (algorithmState != other.algorithmState) return false;
		if (identifier == null)
		{
			if (other.identifier != null) return false;
		}
		else if (!identifier.equals(other.identifier)) return false;
		if (sourceAttributeMetaDatas == null)
		{
			if (other.sourceAttributeMetaDatas != null) return false;
		}
		else if (!sourceAttributeMetaDatas.equals(other.sourceAttributeMetaDatas)) return false;
		if (targetAttributeMetaData == null)
		{
			if (other.targetAttributeMetaData != null) return false;
		}
		else if (!targetAttributeMetaData.equals(other.targetAttributeMetaData)) return false;
		return true;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public void setAlgorithmState(AlgorithmState algorithmState)
	{
		this.algorithmState = algorithmState;
	}

	@Override
	public String toString()
	{
		return "AttributeMapping [identifier=" + identifier + ", targetAttributeMetaData=" + targetAttributeMetaData
				+ ", sourceAttributeMetaDatas=" + sourceAttributeMetaDatas + ", algorithm=" + algorithm
				+ ", algorithmState=" + algorithmState + "]";
	}

	public void setAlgorithm(String algorithm)
	{
		this.algorithm = algorithm;
	}

	AlgorithmState convertToEnum(String enumTypeString)
	{
		if (StringUtils.isNotEmpty(enumTypeString))
		{
			for (AlgorithmState enumType : AlgorithmState.values())
			{
				if (enumType.toString().equalsIgnoreCase(enumTypeString)) return enumType;
			}
		}
		return null;
	}
}
