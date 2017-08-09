package org.molgenis.questionnaires;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;

import java.util.stream.Stream;

import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.questionnaires.QuestionnaireMetaData.QUESTIONNAIRE;

public class QuestionnaireUtils
{
	/**
	 * Get all MetaData entities that extend from QuestionnaireMetaData
	 *
	 * @param dataService
	 * @return
	 */
	public static Stream<EntityType> findQuestionnairesMetaData(DataService dataService)
	{
		return dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
						  .eq(EntityTypeMetadata.EXTENDS, QUESTIONNAIRE)
						  .findAll();
	}
}