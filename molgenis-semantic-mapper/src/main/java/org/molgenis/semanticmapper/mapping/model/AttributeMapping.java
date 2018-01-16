package org.molgenis.semanticmapper.mapping.model;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.meta.model.Attribute;

import java.util.List;

public class AttributeMapping
{
	private String identifier;
	private String targetAttributeName;
	private final Attribute targetAttribute;
	private final List<Attribute> sourceAttributes;
	private String algorithm;
	private AlgorithmState algorithmState;

	public enum AlgorithmState
	{
		CURATED("CURATED"), GENERATED_HIGH("GENERATED_HIGH"), GENERATED_LOW("GENERATED_LOW"), DISCUSS("DISCUSS"), MISSING_TARGET("MISSING_TARGET");

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

	public AttributeMapping(String identifier, String targetAttributeName, Attribute targetAttribute, String algorithm,
			List<Attribute> sourceAttributes)
	{
		this(identifier, targetAttributeName, targetAttribute, algorithm, sourceAttributes, null);
	}

	public AttributeMapping(String identifier, String targetAttributeName, Attribute targetAttribute, String algorithm,
			List<Attribute> sourceAttributes, String algorithmState)
	{
		this.identifier = identifier;
		this.targetAttributeName = targetAttributeName;
		this.targetAttribute = targetAttribute;
		this.sourceAttributes = sourceAttributes;
		this.algorithm = algorithm;
		this.algorithmState = convertToEnum(algorithmState);
	}

	/**
	 * Creates a new empty AttributeMapping
	 *
	 * @param target mapping target attribute
	 */
	public AttributeMapping(Attribute target)
	{
		this.identifier = null;
		this.targetAttributeName = target.getName();
		this.targetAttribute = target;
		this.sourceAttributes = Lists.newArrayList();
		this.algorithm = null;
		this.algorithmState = null;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public String getTargetAttributeName()
	{
		return targetAttributeName;
	}

	public Attribute getTargetAttribute()
	{
		return targetAttribute;
	}

	public List<Attribute> getSourceAttributes()
	{
		return sourceAttributes;
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
		result = prime * result + ((sourceAttributes == null) ? 0 : sourceAttributes.hashCode());
		result = prime * result + ((targetAttribute == null) ? 0 : targetAttribute.hashCode());
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
		if (sourceAttributes == null)
		{
			if (other.sourceAttributes != null) return false;
		}
		else if (!sourceAttributes.equals(other.sourceAttributes)) return false;
		if (targetAttribute == null)
		{
			if (other.targetAttribute != null) return false;
		}
		else if (!targetAttribute.equals(other.targetAttribute)) return false;
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
		return "AttributeMapping [identifier=" + identifier + ", targetAttribute=" + targetAttribute
				+ ", sourceAttributes=" + sourceAttributes + ", algorithm=" + algorithm + ", algorithmState="
				+ algorithmState + "]";
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
