package org.molgenis.omx.study;

import java.util.List;

/**
 * Find, retrieve and persist study definitions
 * 
 * @author erwin
 * 
 */
public interface StudyDefinitionService
{
	/**
	 * Find all study definitions
	 * 
	 * @return
	 */
	public List<StudyDefinitionInfo> findStudyDefinitions();

	/**
	 * Retrieve a study definition and save it in the database
	 * 
	 * @param id
	 */
	public void loadStudyDefinition(String id) throws UnknownStudyDefinitionException;

	/**
	 * Persist a study definition
	 * 
	 * @param studyDefinition
	 */
	public void persistStudyDefinition(StudyDefinition studyDefinition);
}
