package org.molgenis.omx.study;

import java.util.List;

import org.springframework.scheduling.annotation.Async;

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
	 * @throws UnknownStudyDefinitionException
	 */
	public void loadStudyDefinition(String id) throws UnknownStudyDefinitionException;

	/**
	 * Persist a study definition
	 * 
	 * @param studyDefinition
	 */
	@Async
	public void persistStudyDefinition(StudyDefinition studyDefinition);
}
