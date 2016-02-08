package org.molgenis.data.i18n;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.support.DefaultEntityMetaData;

public class LanguageMetaData extends DefaultEntityMetaData
{
	public static final LanguageMetaData INSTANCE = new LanguageMetaData();
	public static final String ENTITY_NAME = "languages";
	public static final String CODE = "code";
	public static final String NAME = "name";

	private LanguageMetaData()
	{
		super(ENTITY_NAME);

		// 2 or 3 characters, alphanumeric, lowercase
		addAttribute(CODE, ROLE_ID).setDescription("Lowercase ISO 639 alpha-2 or alpha-3 code")
				.setValidationExpression("/^[a-z]{2,3}$/.test($('code').value())");
		addAttribute(NAME).setNillable(false);
	}
}
