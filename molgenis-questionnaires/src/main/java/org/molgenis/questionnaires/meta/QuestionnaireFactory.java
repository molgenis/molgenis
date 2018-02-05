package org.molgenis.questionnaires.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class QuestionnaireFactory extends AbstractSystemEntityFactory<Questionnaire, QuestionnaireMetaData, String>
{
	QuestionnaireFactory(QuestionnaireMetaData questionnaireMetaData, EntityPopulator entityPopulator)
	{
		super(Questionnaire.class, questionnaireMetaData, entityPopulator);
	}
}
