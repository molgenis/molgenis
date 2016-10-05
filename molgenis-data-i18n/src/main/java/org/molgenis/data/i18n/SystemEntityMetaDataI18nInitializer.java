package org.molgenis.data.i18n;

import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
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

	@Autowired
	public SystemEntityMetaDataI18nInitializer(MetaDataService metaDataService)
	{
		this.metaDataService = requireNonNull(metaDataService);
	}

	/**
	 * Initialize internationalization attributes
	 *
	 * @param event application event
	 */
	public void initialize(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Stream<String> languageCodes = metaDataService.getDefaultBackend().getLanguageCodes();

		EntityTypeMetadata entityMetaMeta = ctx.getBean(EntityTypeMetadata.class);
		AttributeMetaDataMetaData attrMetaMeta = ctx.getBean(AttributeMetaDataMetaData.class);
		I18nStringMetaData i18nStringMeta = ctx.getBean(I18nStringMetaData.class);

		languageCodes.forEach(languageCode ->
		{
			entityMetaMeta.addAttribute(EntityTypeMetadata.LABEL + '-' + languageCode).setNillable(true)
					.setLabel("Label (" + languageCode + ')');
			entityMetaMeta.addAttribute(EntityTypeMetadata.DESCRIPTION + '-' + languageCode).setNillable(true)
					.setLabel("Description (" + languageCode + ')');
			attrMetaMeta.addAttribute(AttributeMetaDataMetaData.LABEL + '-' + languageCode).setNillable(true)
					.setLabel("Label (" + languageCode + ')');
			attrMetaMeta.addAttribute(AttributeMetaDataMetaData.DESCRIPTION + '-' + languageCode).setNillable(true)
					.setLabel("Description (" + languageCode + ')');
			i18nStringMeta.addAttribute(languageCode).setNillable(true).setDataType(TEXT);
		});
	}
}
