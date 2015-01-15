package org.molgenis.data.mapping;

import org.molgenis.data.AttributeMetaData;

/**
 * Created by charbonb on 14/01/15.
 */
public class AttributeMapping
{
	private String identifier;
	private AttributeMetaData sourceAttribureMetaData;
	private AttributeMetaData targetAttribureMetaData;
	private String algorithm;

	public AttributeMapping(String identifier, AttributeMetaData sourceAttribureMetaData,
			AttributeMetaData targetAttribureMetaData, String algorithm)
	{
		this.identifier = identifier;
		this.sourceAttribureMetaData = sourceAttribureMetaData;
		this.targetAttribureMetaData = targetAttribureMetaData;
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

	public AttributeMetaData getSourceAttribureMetaData()
	{
		return sourceAttribureMetaData;
	}

	public void setSourceAttribureMetaData(AttributeMetaData sourceAttribureMetaData)
	{
		this.sourceAttribureMetaData = sourceAttribureMetaData;
	}

	public AttributeMetaData getTargetAttribureMetaData()
	{
		return targetAttribureMetaData;
	}

	public void setTargetAttribureMetaData(AttributeMetaData targetAttribureMetaData)
	{
		this.targetAttribureMetaData = targetAttribureMetaData;
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
