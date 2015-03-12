package org.molgenis.questionnaires;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.security.owned.OwnedEntityMetaData;
import org.springframework.stereotype.Component;

/**
 * Base EntityMetaData for 'questionnaire' entities
 */
@Component
public class QuestionnaireMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "Questionnaire";
	public static final String ATTR_STATUS = "status";

	public QuestionnaireMetaData()
	{
		super(ENTITY_NAME);
		setAbstract(true);
		setExtends(new OwnedEntityMetaData());
		addAttribute(ATTR_STATUS);
	}
}
