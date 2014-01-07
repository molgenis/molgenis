package org.molgenis.studymanager;

import java.util.List;

import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.study.StudyDefinition;
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
	List<StudyDefinition> getStudyDefinitions();

	/**
	 * Find all study definition with the given status for a user
	 * 
	 * @param username
	 * @param status
	 * @return
	 */
	List<StudyDefinition> getStudyDefinitions(String username, StudyDefinition.Status status);

	/**
	 * Find the study definition with the given id
	 * 
	 * @param id
	 *            study definition id
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
	 *            study definition id
	 * @throws UnknownStudyDefinitionException
	 */
	void loadStudyData(String id) throws UnknownStudyDefinitionException;

	/**
	 * Returns whether study definition data is loaded
	 * 
	 * @param id
	 *            study definition id
	 * @return
	 * @throws UnknownStudyDefinitionException
	 */
	boolean isStudyDataLoaded(String id) throws UnknownStudyDefinitionException;

	/**
	 * Creates a new study definition for the given user based on a catalog
	 * 
	 * @param username
	 * @param catalogId
	 * @return study definition for the given user
	 */
	StudyDefinition createStudyDefinition(String username, String catalogId) throws UnknownCatalogException;

	/**
	 * Updates an existing study definition
	 * 
	 * @param studyDefinition
	 * @throws UnknownStudyDefinitionException
	 */
	void updateStudyDefinition(StudyDefinition studyDefinition) throws UnknownStudyDefinitionException;

	/**
	 * Submit the draft study definition for the given catalog
	 * 
	 * @param id
	 *            study definition id
	 * @throws UnknownStudyDefinitionException
	 */
	void submitStudyDefinition(String id, String catalogId) throws UnknownStudyDefinitionException,
			UnknownCatalogException;
}
