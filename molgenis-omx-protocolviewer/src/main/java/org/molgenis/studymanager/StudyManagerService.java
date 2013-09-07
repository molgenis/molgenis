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
	 * Find the study definition with the given id
	 * 
	 * @return
	 */
	public StudyDefinition getStudyDefinition(String id);

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
	 * @throws UnknownStudyDefinitionException
	 */
	public void loadStudyDefinition(String id) throws UnknownStudyDefinitionException;

	/**
	 * Updates an existing study definition
	 * 
	 * @param studyDefinition
	 * @throws UnknownStudyDefinitionException
	 */
	// public void updateStudyDefinition(StudyDefinition studyDefinition) throws UnknownStudyDefinitionException;

	/**
	 * Persist a study definition
	 * 
	 * @param studyDefinition
	 * @return study definition with id
	 */
	public StudyDefinition persistStudyDefinition(StudyDefinition studyDefinition);
}
