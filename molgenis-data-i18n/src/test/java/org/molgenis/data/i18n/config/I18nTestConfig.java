package org.molgenis.data.i18n.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.i18n.model.I18nStringFactory;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.i18n.model.LanguageMetadata;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ EntityBaseTestConfig.class, I18nStringMetaData.class, I18nStringFactory.class, LanguageMetadata.class,
		LanguageFactory.class })
public class I18nTestConfig
{

}
