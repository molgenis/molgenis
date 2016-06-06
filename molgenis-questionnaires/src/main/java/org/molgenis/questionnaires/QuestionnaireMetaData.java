package org.molgenis.questionnaires;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.meta.RootSystemPackage.PACKAGE_SYSTEM;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Base EntityMetaData for 'questionnaire' entities
 */
@Component
public class QuestionnaireMetaData extends SystemEntityMetaDataImpl
{
	private static final String SIMPLE_NAME = "Questionnaire";
	public static final String QUESTIONNAIRE = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ATTR_STATUS = "status";

	private OwnedEntityMetaData ownedEntityMetaData;

	QuestionnaireMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
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
		addAttribute(ATTR_STATUS).setDataType(new EnumField()).setEnumOptions(enumOptions).setVisible(false)
				.setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setOwnedEntityMetaData(OwnedEntityMetaData ownedEntityMetaData)
	{
		this.ownedEntityMetaData = requireNonNull(ownedEntityMetaData);
	}
}
