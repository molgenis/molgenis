package org.molgenis.data.mapper.controller;

public class FirstAttributeMappingInfo
{
	/**
	 * @return the mappingProjectId
	 */
	public String getMappingProjectId()
	{
		return mappingProjectId;
	}

	/**
	 * @return the target
	 */
	public String getTarget()
	{
		return target;
	}

	/**
	 * @return the source
	 */
	public String getSource()
	{
		return source;
	}

	/**
	 * @return the targetAttribute
	 */
	public String getTargetAttribute()
	{
		return targetAttribute;
	}

	private String mappingProjectId;
	private String target;
	private String source;
	private String targetAttribute;

	public FirstAttributeMappingInfo(String mappingProjectId, String target, String source, String targetAttribute)
	{
		this.mappingProjectId = mappingProjectId;
		this.target = target;
		this.source = source;
		this.targetAttribute = targetAttribute;
	}
}
