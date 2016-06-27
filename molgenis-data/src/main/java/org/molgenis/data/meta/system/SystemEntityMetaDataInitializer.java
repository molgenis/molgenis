package org.molgenis.data.meta.system;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.stream.Stream;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.i18n.I18nStringMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.data.meta.model.MetaPackage;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Discovers and initializes {@link SystemEntityMetaData} beans.
 */
@Component
public class SystemEntityMetaDataInitializer
{
	private final MetaDataService metaDataService;
	private final RootSystemPackage rootSystemPackage;
	private final MetaPackage metaPackage;

	@Autowired
	public SystemEntityMetaDataInitializer(MetaDataService metaDataService, RootSystemPackage rootSystemPackage,
			MetaPackage metaPackage)
	{
		this.metaDataService = requireNonNull(metaDataService);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
		this.metaPackage = requireNonNull(metaPackage);
	}

	public void initialize(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();

		ctx.getBeansOfType(SystemPackage.class).values().forEach(SystemPackage::bootstrap);

		EntityMetaDataMetaData entityMetaDataMetaData = ctx.getBean(EntityMetaDataMetaData.class);
		ctx.getBean(AttributeMetaDataMetaData.class).bootstrap(entityMetaDataMetaData);
		Map<String, SystemEntityMetaData> systemEntityMetaDataMap = ctx.getBeansOfType(SystemEntityMetaData.class);
		//FIXME: can we do this cleaner than with the hardcoded "Owned". Problem: even the "isAbstract" nullpoiters at this point.
		systemEntityMetaDataMap.values().stream().filter(systemEntityMetaData -> systemEntityMetaData.getSimpleName().equals("Owned"))
				.forEach(systemEntityMetaData -> initialize(systemEntityMetaData, entityMetaDataMetaData));
		systemEntityMetaDataMap.values().stream().filter(systemEntityMetaData -> !systemEntityMetaData.getSimpleName().equals("Owned"))
				.forEach(systemEntityMetaData -> initialize(systemEntityMetaData, entityMetaDataMetaData));

		initializeI18N(ctx);
	}

	/**
	 * Initialize internationalization attributes
	 *
	 * @param ctx application context
	 */
	private void initializeI18N(ApplicationContext ctx)
	{
		Stream<String> languageCodes = metaDataService.getDefaultBackend().getLanguageCodes();

		EntityMetaDataMetaData entityMetaMeta = ctx.getBean(EntityMetaDataMetaData.class);
		AttributeMetaDataMetaData attrMetaMeta = ctx.getBean(AttributeMetaDataMetaData.class);
		I18nStringMetaData i18nStringMeta = ctx.getBean(I18nStringMetaData.class);

		languageCodes.forEach(languageCode -> {
			entityMetaMeta.addAttribute(EntityMetaDataMetaData.LABEL + '-' + languageCode).setNillable(true).setLabel(
					new StringBuilder().append("Label (").append(languageCode).append(")").toString());
			entityMetaMeta.addAttribute(EntityMetaDataMetaData.DESCRIPTION + '-' + languageCode).setNillable(true).setLabel(
					new StringBuilder().append("Description (").append(languageCode).append(")").toString());
			attrMetaMeta.addAttribute(AttributeMetaDataMetaData.LABEL + '-' + languageCode).setNillable(true).setLabel(
					new StringBuilder().append("Label (").append(languageCode).append(")").toString());
			attrMetaMeta.addAttribute(AttributeMetaDataMetaData.DESCRIPTION + '-' + languageCode).setNillable(true).setLabel(
					new StringBuilder().append("Description (").append(languageCode).append(")").toString());
			i18nStringMeta.addAttribute(languageCode).setNillable(true).setDataType(MolgenisFieldTypes.TEXT);
		});
	}

	private void initialize(SystemEntityMetaData systemEntityMetaData, EntityMetaDataMetaData entityMetaDataMetaData)
	{
		systemEntityMetaData.bootstrap(entityMetaDataMetaData);
		if (systemEntityMetaData.getBackend() == null)
		{
			systemEntityMetaData.setBackend(metaDataService.getDefaultBackend().getName());
		}
		if (systemEntityMetaData.getPackage() == null)
		{
			if (metaDataService.isMetaEntityMetaData(systemEntityMetaData))
			{
				systemEntityMetaData.setPackage(metaPackage);
			}
			else
			{
				systemEntityMetaData.setPackage(rootSystemPackage);
			}
		}
		else if (!systemEntityMetaData.getPackage().getRootPackage().getName().equals(rootSystemPackage.getName()))
		{
			throw new RuntimeException(
					format("System entity [%s] must be in package [%s]", systemEntityMetaData.getName(),
							rootSystemPackage.getName()));
		}
	}
}
