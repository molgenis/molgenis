package org.molgenis.data.mapping;

import org.molgenis.data.AttributeMetaData;

/**
 * Created by charbonb on 14/01/15.
 */
public class AttributeMapping
{
	private String identifier;
	private AttributeMetaData sourceAttributeMetaData;
	private AttributeMetaData targetAttributeMetaData;
	private String algorithm;

	public AttributeMapping(String identifier, AttributeMetaData sourceAttributeMetaData,
			AttributeMetaData targetAttributeMetaData, String algorithm)
	{
		this.identifier = identifier;
		this.sourceAttributeMetaData = sourceAttributeMetaData;
		this.targetAttributeMetaData = targetAttributeMetaData;
		this.algorithm = algorithm;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public AttributeMetaData getSourceAttributeMetaData()
	{
		return sourceAttributeMetaData;
	}

	public void setSourceAttributeMetaData(AttributeMetaData sourceAttributeMetaData)
	{
		this.sourceAttributeMetaData = sourceAttributeMetaData;
	}

	public AttributeMetaData getTargetAttributeMetaData()
	{
		return targetAttributeMetaData;
	}

	public void setTargetAttributeMetaData(AttributeMetaData targetAttributeMetaData)
	{
		this.targetAttributeMetaData = targetAttributeMetaData;
	}

	public String getAlgorithm()
	{
		return algorithm;
	}

	public void setAlgorithm(String algorithm)
	{
		this.algorithm = algorithm;
	}
}
