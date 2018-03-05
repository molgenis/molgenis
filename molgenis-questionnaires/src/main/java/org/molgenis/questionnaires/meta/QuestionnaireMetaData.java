package org.molgenis.questionnaires.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.security.owned.OwnedEntityType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.molgenis.questionnaires.meta.QuestionnaireStatus.SUBMITTED;

/**
 * Base EntityType for 'questionnaire' entities
 */
@Component
public class QuestionnaireMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Questionnaire";
	public static final String QUESTIONNAIRE = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ATTR_STATUS = "status";
	public static final String SUBMIT_DATE = "submitDate";

	private final OwnedEntityType ownedEntityType;

	QuestionnaireMetaData(OwnedEntityType ownedEntityType)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.ownedEntityType = requireNonNull(ownedEntityType);
	}

	@Override
	public void init()
	{
		setLabel("Questionnaire");
		setAbstract(true);
		setExtends(ownedEntityType);

		List<String> enumOptions = new ArrayList<>();
		for (QuestionnaireStatus questionnaireStatus : QuestionnaireStatus.values())
		{
			enumOptions.add(questionnaireStatus.toString());
		}

		addAttribute(ATTR_STATUS).setDataType(ENUM).setEnumOptions(enumOptions).setVisible(false).setNillable(false);

		// Attribute turns required and visible when questionnaire is submitted
		addAttribute(SUBMIT_DATE).setDataType(DATE_TIME)
								 .setLabel("Submit date")
								 .setNullableExpression(
										 "$('" + ATTR_STATUS + "').value() !== '" + SUBMITTED.toString() + "'")
								 .setVisibleExpression(
										 "$('" + ATTR_STATUS + "').value() === '" + SUBMITTED.toString() + "'");
	}
}
