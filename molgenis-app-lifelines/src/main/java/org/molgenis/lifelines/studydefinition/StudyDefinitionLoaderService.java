package org.molgenis.lifelines.studydefinition;

import java.util.List;

/**
 * Find and retrieve studydefinitions
 * 
 * @author erwin
 * 
 */
public interface StudyDefinitionLoaderService
{
	/**
	 * Find all studyfinitions
	 * 
	 * @return
	 */
	public List<StudyDefinitionInfo> findStudyDefinitions();

	/**
	 * Retrieve a studydefinition and save it in the database
	 * 
	 * @param id
	 */
	public void loadStudyDefinition(String id) throws UnknownStudyDefinitionException;
}
