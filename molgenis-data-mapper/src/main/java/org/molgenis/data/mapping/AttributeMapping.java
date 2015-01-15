package org.molgenis.data.mapping;

import org.molgenis.data.AttributeMetaData;

/**
 * Created by charbonb on 14/01/15.
 */
public class AttributeMapping
{
	private String id;
	private AttributeMetaData sourceAttribureMetaData;
	private AttributeMetaData targetAttribureMetaData;
	private String algorithm;

	public AttributeMapping(String id, AttributeMetaData sourceAttribureMetaData,
			AttributeMetaData targetAttribureMetaData, String algorithm)
	{
		this.id = id;
		this.sourceAttribureMetaData = sourceAttribureMetaData;
		this.targetAttribureMetaData = targetAttribureMetaData;
		this.algorithm = algorithm;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
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
