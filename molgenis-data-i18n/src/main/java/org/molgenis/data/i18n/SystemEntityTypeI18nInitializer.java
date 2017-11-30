package org.molgenis.data.i18n;

import org.molgenis.data.i18n.model.L10nStringMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.i18n.LanguageService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.support.AttributeUtils.getI18nAttributeName;

/**
 * Enriches system entity meta data with internationalization attributes.
 *
 * @see SystemEntityType
 */
@Component
public class SystemEntityTypeI18nInitializer
{
	/**
	 * Initialize internationalization attributes
	 *
	 * @param event application event
	 */
	public void initialize(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Stream<String> languageCodes = LanguageService.getLanguageCodes();

		EntityTypeMetadata entityTypeMeta = ctx.getBean(EntityTypeMetadata.class);
		AttributeMetadata attrMetaMeta = ctx.getBean(AttributeMetadata.class);
		L10nStringMetaData l10nStringMeta = ctx.getBean(L10nStringMetaData.class);

		languageCodes.forEach(languageCode ->
		{
			entityTypeMeta.addAttribute(getI18nAttributeName(EntityTypeMetadata.LABEL, languageCode))
						  .setNillable(true)
						  .setLabel("Label (" + languageCode + ')');
			entityTypeMeta.addAttribute(getI18nAttributeName(EntityTypeMetadata.DESCRIPTION, languageCode))
						  .setNillable(true)
						  .setLabel("Description (" + languageCode + ')')
						  .setDataType(TEXT);
			attrMetaMeta.addAttribute(getI18nAttributeName(AttributeMetadata.LABEL, languageCode))
						.setNillable(true)
						.setLabel("Label (" + languageCode + ')');
			attrMetaMeta.addAttribute(getI18nAttributeName(AttributeMetadata.DESCRIPTION, languageCode))
						.setNillable(true)
						.setLabel("Description (" + languageCode + ')')
						.setDataType(TEXT);
			l10nStringMeta.addAttribute(languageCode).setNillable(true).setDataType(TEXT);
		});
	}
}
