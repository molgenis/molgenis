package org.molgenis.data.mapping.model;

import org.molgenis.data.AttributeMetaData;

/**
 * Created by charbonb on 14/01/15.
 */
public class AttributeMapping
{
	private String identifier;
	private AttributeMetaData sourceAttributeMetaData;
	private final AttributeMetaData targetAttributeMetaData;
	private String algorithm;

	public AttributeMapping(String identifier, AttributeMetaData sourceAttributeMetaData,
			AttributeMetaData targetAttributeMetaData, String algorithm)
	{
		this.identifier = identifier;
		this.sourceAttributeMetaData = sourceAttributeMetaData;
		this.targetAttributeMetaData = targetAttributeMetaData;
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
		this.sourceAttributeMetaData = null;
		this.targetAttributeMetaData = target;
		this.algorithm = null;
	}

	/**
	 * Sets the source, this will set the algorithm to a direct map.
	 * 
	 * @param attributeMetaData the source attribute
	 */
	public void setSource(AttributeMetaData source)
	{
		this.sourceAttributeMetaData = source;
		this.algorithm = "${" + sourceAttributeMetaData.getName() + "}";
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public AttributeMetaData getSourceAttributeMetaData()
	{
		return sourceAttributeMetaData;
	}

	public AttributeMetaData getTargetAttributeMetaData()
	{
		return targetAttributeMetaData;
	}

	public String getAlgorithm()
	{
		return algorithm;
	}

}
