package org.molgenis.data.i18n;

import org.molgenis.data.DataService;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.MolgenisFieldTypes.AttributeType.TEXT;

/**
 * Enriches system entity meta data with internationalization attributes.
 *
 * @see SystemEntityMetaData
 */
@Component
public class SystemEntityMetaDataI18nInitializer
{
	private final MetaDataService metaDataService;
	private final DataService dataService;
	private final LanguageFactory languageFactory;

	@Autowired
	public SystemEntityMetaDataI18nInitializer(MetaDataService metaDataService, DataService dataService,
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

		EntityMetaDataMetaData entityMetaMeta = ctx.getBean(EntityMetaDataMetaData.class);
		AttributeMetaData attrMetaMeta = ctx.getBean(AttributeMetaData.class);
		I18nStringMetaData i18nStringMeta = ctx.getBean(I18nStringMetaData.class);

		languageCodes.forEach(languageCode ->
		{
			entityMetaMeta.addAttribute(EntityMetaDataMetaData.LABEL + '-' + languageCode).setNillable(true)
					.setLabel("Label (" + languageCode + ')');
			entityMetaMeta.addAttribute(EntityMetaDataMetaData.DESCRIPTION + '-' + languageCode).setNillable(true)
					.setLabel("Description (" + languageCode + ')').setDataType(TEXT);
			attrMetaMeta.addAttribute(AttributeMetaData.LABEL + '-' + languageCode).setNillable(true)
					.setLabel("Label (" + languageCode + ')');
			attrMetaMeta.addAttribute(AttributeMetaData.DESCRIPTION + '-' + languageCode).setNillable(true)
					.setLabel("Description (" + languageCode + ')').setDataType(TEXT);
			i18nStringMeta.addAttribute(languageCode).setNillable(true).setDataType(STRING);
		});
	}
}
