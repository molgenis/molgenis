package org.molgenis.omx.study;

import java.util.List;

public interface StudyDefinition
{
	String getId();

	void setId(String id);

	String getName();

	String getDescription();

	String getCatalogVersion();

	Iterable<StudyDefinitionItem> getItems();

	boolean containsItem(StudyDefinitionItem item);

	List<String> getAuthors();

	String getAuthorEmail();
}
