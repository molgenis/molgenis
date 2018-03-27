package org.molgenis.integrationtest.config;

import org.molgenis.data.i18n.LocalizationService;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.data.i18n.model.L10nStringMetaData;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.i18n.model.LanguageMetadata;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ LocalizationService.class, L10nStringFactory.class, L10nStringMetaData.class, LanguageFactory.class,
		LanguageMetadata.class })
public class LanguageTestConfig
{
}
