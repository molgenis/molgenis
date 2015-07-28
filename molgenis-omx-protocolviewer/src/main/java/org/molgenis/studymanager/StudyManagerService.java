package org.molgenis.studymanager;

import java.util.List;

import org.molgenis.study.StudyDefinition;
import org.molgenis.study.StudyDefinitionMeta;
import org.molgenis.study.UnknownStudyDefinitionException;

/**
 * Find, retrieve and persist study definitions
 * 
 * @author erwin
 * 
 */
public interface StudyManagerService
{
	/**
	 * Find all study definitions
	 * 
	 * @return
	 */
	List<StudyDefinitionMeta> getStudyDefinitions();

	/**
	 * Find the study definition with the given id
	 * 
	 * @param id
	 * @return
	 * @throws UnknownStudyDefinitionException
	 */
	StudyDefinition getStudyDefinition(String id) throws UnknownStudyDefinitionException;

	/**
	 * Returns whether study data loading is enabled
	 * 
	 * @return
	 */
	boolean canLoadStudyData();

	/**
	 * Retrieve a study definition and save it in the database
	 * 
	 * @param id
	 * @throws UnknownStudyDefinitionException
	 */
	void loadStudyData(String id) throws UnknownStudyDefinitionException;

	/**
	 * Returns whether study definition data is loaded
	 * 
	 * @param id
	 * @return
	 * @throws UnknownStudyDefinitionException
	 */
	boolean isStudyDataLoaded(String id) throws UnknownStudyDefinitionException;

	/**
	 * Updates an existing study definition
	 * 
	 * @param studyDefinition
	 * @throws UnknownStudyDefinitionException
	 */
	void updateStudyDefinition(StudyDefinition studyDefinition) throws UnknownStudyDefinitionException;

	/**
	 * Persist a study definition
	 * 
	 * @param studyDefinition
	 * @return study definition with id
	 */
	StudyDefinition persistStudyDefinition(StudyDefinition studyDefinition);
}
