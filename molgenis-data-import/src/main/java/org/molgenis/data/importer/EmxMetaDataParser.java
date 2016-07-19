package org.molgenis.data.importer;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.framework.db.EntitiesValidationReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.importer.EmxMetaDataParserUtils.*;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;

/**
 * Parser for the EMX metadata. This class is stateless, but it passes state between methods using
 * {@link IntermediateParseResults}.
 *
 * This class is constructed with a dataservice
 */
public class EmxMetaDataParser implements MetaDataParser
{
	private final DataService dataService;
	private final PackageFactory packageFactory;
	private final AttributeMetaDataFactory attrMetaFactory;
	private final EntityMetaDataFactory entityMetaDataFactory;
	private final EmxMetaDataParserUtils emxMetaDataParserUtils;

	public EmxMetaDataParser(PackageFactory packageFactory, AttributeMetaDataFactory attrMetaFactory,
			EntityMetaDataFactory entityMetaDataFactory)
	{
		this.dataService = null;
		this.packageFactory = requireNonNull(packageFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.entityMetaDataFactory = entityMetaDataFactory;
		this.emxMetaDataParserUtils = new EmxMetaDataParserUtils(packageFactory, attrMetaFactory, entityMetaDataFactory,
				null);
	}

	public EmxMetaDataParser(DataService dataService, PackageFactory packageFactory,
			AttributeMetaDataFactory attrMetaFactory, EntityMetaDataFactory entityMetaDataFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.packageFactory = requireNonNull(packageFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.entityMetaDataFactory = entityMetaDataFactory;
		this.emxMetaDataParserUtils = new EmxMetaDataParserUtils(packageFactory, attrMetaFactory, entityMetaDataFactory,
				dataService);
	}

	@Override
	public ParsedMetaData parse(final RepositoryCollection source, String defaultPackage)
	{
		if (source.getRepository(EMX_ATTRIBUTES) != null)
		{
			IntermediateParseResults intermediateResults = emxMetaDataParserUtils.getEntityMetaDataFromSource(source);
			List<EntityMetaData> entities;
			if ((defaultPackage == null) || PACKAGE_DEFAULT.equalsIgnoreCase(defaultPackage))
			{
				entities = intermediateResults.getEntities();
			}
			else
			{
				entities = emxMetaDataParserUtils.putEntitiesInDefaultPackage(intermediateResults, defaultPackage);
			}

			return new ParsedMetaData(emxMetaDataParserUtils.resolveEntityDependencies(entities),
					intermediateResults.getPackages(), intermediateResults.getAttributeTags(),
					intermediateResults.getEntityTags(), intermediateResults.getLanguages(),
					intermediateResults.getI18nStrings());
		}
		else
		{
			if (dataService != null)
			{
				List<EntityMetaData> metadataList = new ArrayList<>();
				for (String emxName : source.getEntityNames())
				{
					String repoName = EMX_NAME_TO_REPO_NAME_MAP.get(emxName);
					if (repoName == null) repoName = emxName;
					metadataList.add(dataService.getRepository(repoName).getEntityMetaData());
				}
				IntermediateParseResults intermediateResults = emxMetaDataParserUtils
						.parseTagsSheet(source.getRepository(EMX_TAGS));
				emxMetaDataParserUtils.parsePackagesSheet(source.getRepository(EMX_PACKAGES), intermediateResults);
				emxMetaDataParserUtils.parsePackageTags(source.getRepository(EMX_PACKAGES), intermediateResults);

				if (source.hasRepository(EMX_LANGUAGES))
				{
					emxMetaDataParserUtils.parseLanguages(source.getRepository(EMX_LANGUAGES), intermediateResults);
				}

				if (source.hasRepository(EMX_I18NSTRINGS))
				{
					emxMetaDataParserUtils.parseI18nStrings(source.getRepository(EMX_I18NSTRINGS), intermediateResults);
				}

				return new ParsedMetaData(emxMetaDataParserUtils.resolveEntityDependencies(metadataList),
						intermediateResults.getPackages(), intermediateResults.getAttributeTags(),
						intermediateResults.getEntityTags(), intermediateResults.getLanguages(),
						intermediateResults.getI18nStrings());
			}
			else
			{
				throw new UnsupportedOperationException();
			}
		}
	}

	@Override
	public EntitiesValidationReport validate(RepositoryCollection source)
	{
		MyEntitiesValidationReport report = new MyEntitiesValidationReport();
		Map<String, EntityMetaData> metaDataMap = emxMetaDataParserUtils.getEntityMetaDataMap(dataService, source);
		return emxMetaDataParserUtils.buildValidationReport(source, report, metaDataMap);
	}
}
