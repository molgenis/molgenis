package org.molgenis.questionnaires;

import java.util.stream.Stream;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaDataMetaData;

public class QuestionnaireUtils
{
	/**
	 * Get all MetaData entities that extend from QuestionnaireMetaData
	 * 
	 * @param dataService
	 * @return
	 */
	public static Stream<Entity> findQuestionnairesMetaData(DataService dataService)
	{
		return dataService.query(EntityMetaDataMetaData.ENTITY_NAME)
				.eq(EntityMetaDataMetaData.EXTENDS, QuestionnaireMetaData.ENTITY_NAME).findAll();
	}
}
