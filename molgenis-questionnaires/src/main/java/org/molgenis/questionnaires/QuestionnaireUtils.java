package org.molgenis.questionnaires;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;

import java.util.stream.Stream;

import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.questionnaires.QuestionnaireMetaData.QUESTIONNAIRE;

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
		return dataService.query(ENTITY_META_DATA).eq(EntityMetaDataMetaData.EXTENDS, QUESTIONNAIRE).findAll();
	}
}
