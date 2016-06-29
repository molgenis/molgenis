package org.molgenis.data.i18n;

import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class LanguageMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "languages";
	public static final String LANGUAGE = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String DEFAULT_LANGUAGE_CODE = "en";
	public static final String DEFAULT_LANGUAGE_NAME = "English";

	public static final String CODE = "code";
	public static final String NAME = "name";

	LanguageMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		// 2 or 3 characters, alphanumeric, lowercase
		addAttribute(CODE, ROLE_ID).setDescription("Lowercase ISO 639 alpha-2 or alpha-3 code")
				.setValidationExpression("/^[a-z]{2,3}$/.test($('code').value())");
		addAttribute(NAME).setNillable(false);
	}
}
