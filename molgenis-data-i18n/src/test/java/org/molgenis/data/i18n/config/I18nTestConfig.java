package org.molgenis.data.i18n.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.data.i18n.model.L10nStringMetaData;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.i18n.model.LanguageMetadata;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, L10nStringMetaData.class, L10nStringFactory.class, LanguageMetadata.class,
		LanguageFactory.class })
public class I18nTestConfig
{

}
