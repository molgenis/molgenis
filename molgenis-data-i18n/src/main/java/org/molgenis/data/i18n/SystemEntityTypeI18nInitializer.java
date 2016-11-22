package org.molgenis.data.i18n;

import org.molgenis.data.DataService;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;

/**
 * Enriches system entity meta data with internationalization attributes.
 *
 * @see SystemEntityType
 */
@Component
public class SystemEntityTypeI18nInitializer
{
	private final MetaDataService metaDataService;
	private final DataService dataService;
	private final LanguageFactory languageFactory;

	@Autowired
	public SystemEntityTypeI18nInitializer(MetaDataService metaDataService, DataService dataService,
			LanguageFactory languageFactory)
	{
		this.metaDataService = requireNonNull(metaDataService);
		this.dataService = requireNonNull(dataService);
		this.languageFactory = requireNonNull(languageFactory);
	}

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
		I18nStringMetaData i18nStringMeta = ctx.getBean(I18nStringMetaData.class);

		languageCodes.forEach(languageCode ->
		{
			entityTypeMeta.addAttribute(EntityTypeMetadata.LABEL + '-' + languageCode).setNillable(true)
					.setLabel("Label (" + languageCode + ')');
			entityTypeMeta.addAttribute(EntityTypeMetadata.DESCRIPTION + '-' + languageCode).setNillable(true)
					.setLabel("Description (" + languageCode + ')').setDataType(TEXT);
			attrMetaMeta.addAttribute(AttributeMetadata.LABEL + '-' + languageCode).setNillable(true)
					.setLabel("Label (" + languageCode + ')');
			attrMetaMeta.addAttribute(AttributeMetadata.DESCRIPTION + '-' + languageCode).setNillable(true)
					.setLabel("Description (" + languageCode + ')').setDataType(TEXT);
			i18nStringMeta.addAttribute(languageCode).setNillable(true).setDataType(STRING);
		});
	}
}
