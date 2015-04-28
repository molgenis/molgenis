package org.molgenis.data.mapper.controller;

import org.molgenis.data.mapper.mapping.model.MappingProject;

/**
 * Request params for the /attributeMapping entry
 * 
 * @param mappingProjectId
 *            ID of the {@link MappingProject}
 * @param target
 *            name of the target entity
 * @param source
 *            name of the source entity
 * @param attribute
 *            name of the target attribute
 * @param isShowSuggestedAttributes
 *            should the attributes be chosen by the user or semantic search must be used to do that
 */
public class RequestAttributeMapping
{
	private String mappingProjectId;
	private String target;
	private String source;
	private String attribute;
	private Boolean showSuggestedAttributes;

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
	 * @return the attribute
	 */
	public String getAttribute()
	{
		return attribute;
	}

	/**
	 * @return the showSuggestedAttributes
	 */
	public boolean isShowSuggestedAttributes()
	{
		return showSuggestedAttributes;
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
	 * @param attribute
	 *            the attribute to set
	 */
	public void setAttribute(String attribute)
	{
		this.attribute = attribute;
	}

	/**
	 * @param showSuggestedAttributes
	 *            the showSuggestedAttributes to set
	 */
	public void setShowSuggestedAttributes(boolean showSuggestedAttributes)
	{
		this.showSuggestedAttributes = showSuggestedAttributes;
	}
}
