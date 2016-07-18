package org.molgenis.data.importer;

import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.framework.db.EntitiesValidationReport;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.importer.EmxMetaDataParserUtils.EMX_ATTRIBUTES;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;

/**
 * Parser for the EMX metadata. This class is stateless, but it passes state between methods using
 * {@link IntermediateParseResults}.
 *
 * This class is constructed without a dataservice
 */
public class EmxFileOnlyMetaDataParser implements MetaDataParser
{
	private final PackageFactory packageFactory;
	private final AttributeMetaDataFactory attrMetaFactory;
	private final EntityMetaDataFactory entityMetaDataFactory;

	private final EmxMetaDataParserUtils emxMetaDataParserUtils;

	public EmxFileOnlyMetaDataParser(PackageFactory packageFactory, AttributeMetaDataFactory attrMetaFactory,
			EntityMetaDataFactory entityMetaDataFactory)
	{
		this.packageFactory = requireNonNull(packageFactory);
		this.entityMetaDataFactory = entityMetaDataFactory;
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.emxMetaDataParserUtils = new EmxMetaDataParserUtils(packageFactory, attrMetaFactory, entityMetaDataFactory,
				null);
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
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public EntitiesValidationReport validate(RepositoryCollection source)
	{
		MyEntitiesValidationReport report = new MyEntitiesValidationReport();
		Map<String, EntityMetaData> metaDataMap = emxMetaDataParserUtils.getEntityMetaDataMap(null, source);
		return emxMetaDataParserUtils.buildValidationReport(source, report, metaDataMap);
	}
}
