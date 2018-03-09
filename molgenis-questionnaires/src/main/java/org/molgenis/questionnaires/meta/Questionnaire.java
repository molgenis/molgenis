package org.molgenis.questionnaires.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.i18n.LanguageService.getCurrentUserLanguageCode;
import static org.molgenis.questionnaires.meta.QuestionnaireMetaData.ATTR_STATUS;
import static org.molgenis.questionnaires.meta.QuestionnaireMetaData.OWNER_USERNAME;

public class Questionnaire extends StaticEntity
{
	public Questionnaire(Entity entity)
	{
		super(entity);
	}

	public Questionnaire(EntityType entityType)
	{
		super(entityType);
	}

	public String getId()
	{
		return getString(getEntityType().getId());
	}

	public QuestionnaireStatus getStatus()
	{
		return QuestionnaireStatus.valueOf(getString(ATTR_STATUS));
	}

	public String getLabel()
	{
		return getEntityType().getLabel(getCurrentUserLanguageCode());
	}

	public String getDescription()
	{
		return getEntityType().getDescription(getCurrentUserLanguageCode());
	}

	public void setStatus(QuestionnaireStatus questionnaireStatus)
	{
		set(ATTR_STATUS, questionnaireStatus.toString());
	}

	public void setOwner(String owner)
	{
		set(OWNER_USERNAME, owner);
	}
}
