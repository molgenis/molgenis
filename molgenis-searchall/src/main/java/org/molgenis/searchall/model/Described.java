package org.molgenis.searchall.model;

import javax.annotation.Nullable;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

public interface Described
{
	String getLabel();

	@Nullable
	String getDescription();

	default boolean isLabelOrDescriptionMatch(String searchterm)
	{
		return isLabelMatch(searchterm) || isDescriptionMatch(searchterm);
	}

	default boolean isLabelMatch(String searchterm)
	{
		return containsIgnoreCase(getLabel(), searchterm);
	}

	default boolean isDescriptionMatch(String searchterm)
	{
		return containsIgnoreCase(getDescription(), searchterm);
	}
}
