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

	/**
	 * @param mappingProjectId
	 *            the mappingProjectId to set
	 */
	public void setMappingProjectId(String mappingProjectId)
	{
		this.mappingProjectId = mappingProjectId;
	}

	/**
	 * @param target
	 *            the target to set
	 */
	public void setTarget(String target)
	{
		this.target = target;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(String source)
	{
		this.source = source;
	}

	/**
	 * @param targetAttribute
	 *            the targetAttribute to set
	 */
	public void setTargetAttribute(String targetAttribute)
	{
		this.targetAttribute = targetAttribute;
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
