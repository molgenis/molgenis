package org.molgenis.questionnaires;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
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

		List<String> enumOptions = new ArrayList<String>();
		for (QuestionnaireStatus questionnaireStatus : QuestionnaireStatus.values())
		{
			enumOptions.add(questionnaireStatus.toString());
		}
		addAttribute(ATTR_STATUS).setDataType(new EnumField()).setEnumOptions(enumOptions).setVisible(false)
				.setNillable(false);
	}
}
