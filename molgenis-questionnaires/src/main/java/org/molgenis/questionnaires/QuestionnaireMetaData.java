package org.molgenis.questionnaires;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.security.owned.OwnedEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.ENUM;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

/**
 * Base EntityMetaData for 'questionnaire' entities
 */
@Component
public class QuestionnaireMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "Questionnaire";
	public static final String QUESTIONNAIRE = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ATTR_STATUS = "status";

	private final OwnedEntityMetaData ownedEntityMetaData;

	@Autowired
	QuestionnaireMetaData(OwnedEntityMetaData ownedEntityMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.ownedEntityMetaData = requireNonNull(ownedEntityMetaData);
	}

	@Override
	public void init()
	{
		setAbstract(true);
		setExtends(ownedEntityMetaData);

		List<String> enumOptions = new ArrayList<>();
		for (QuestionnaireStatus questionnaireStatus : QuestionnaireStatus.values())
		{
			enumOptions.add(questionnaireStatus.toString());
		}
		addAttribute(ATTR_STATUS).setDataType(ENUM).setEnumOptions(enumOptions).setVisible(false).setNillable(false);
	}
}
