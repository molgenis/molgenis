package org.molgenis.data.i18n;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.stereotype.Component;

@Component
public class LanguageMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "languages";
	public static final String CODE = "code";
	public static final String NAME = "name";

	private LanguageMetaData()
	{
	}

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		// 2 or 3 characters, alphanumeric, lowercase
		addAttribute(CODE, ROLE_ID).setDescription("Lowercase ISO 639 alpha-2 or alpha-3 code")
				.setValidationExpression("/^[a-z]{2,3}$/.test($('code').value())");
		addAttribute(NAME).setNillable(false);
	}
}
