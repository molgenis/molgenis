package org.molgenis.omx.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.AggregateableCrudRepositorySecurityDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.omx.OmxRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.QueryResolver;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.EntitiesImporter;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.protocol.OmxLookupTableEntityMetaData;
import org.molgenis.omx.protocol.OmxLookupTableRepository;
import org.molgenis.omx.utils.ProtocolUtils;
import org.molgenis.search.SearchService;
import org.molgenis.util.RepositoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

@Service
public class OmxImporterServiceImpl implements OmxImporterService
{
	private final DataService dataService;
	private final SearchService searchService;
	private final EntitiesImporter entitiesImporter;
	private final EntityValidator entityValidator;
	private final QueryResolver queryResolver;

	@Autowired
	public OmxImporterServiceImpl(DataService dataService, SearchService searchService,
			EntitiesImporter entitiesImporter, EntityValidator entityValidator, QueryResolver queryResolver)
	{
		this.dataService = dataService;
		this.searchService = searchService;
		this.entitiesImporter = entitiesImporter;
		this.entityValidator = entityValidator;
		this.queryResolver = queryResolver;
	}

	@Override
	@Transactional(rollbackFor = IOException.class)
	public EntityImportReport doImport(RepositoryCollection repositories, DatabaseAction databaseAction)
			throws IOException
	{
		// All new repository identifiers
		List<String> newRepoIdentifiers = new ArrayList<String>();

		// First import entities, the data sheets are ignored in the entitiesimporter
		EntityImportReport importReport = entitiesImporter.importEntities(repositories, databaseAction);

		// RULE: Feature can only belong to one Protocol in a DataSet. Check it (see issue #1136)
		checkFeatureCanOnlyBelongToOneProtocolForOneDataSet();

		// Import data sheets
		for (String name : repositories.getEntityNames())
		{
			Repository repository = repositories.getRepositoryByEntityName(name);

			if (repository.getName().startsWith(DATASET_SHEET_PREFIX))
			{
				// Import DataSet sheet, create new OmxRepository
				String identifier = repository.getName().substring(DATASET_SHEET_PREFIX.length());

				if (!dataService.hasRepository(identifier))
				{

					dataService.addRepository(new AggregateableCrudRepositorySecurityDecorator(new OmxRepository(
							dataService, searchService, identifier, entityValidator)));
					newRepoIdentifiers.add(identifier);

					DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
							new QueryImpl().eq(DataSet.IDENTIFIER, identifier), DataSet.class);

					List<Protocol> protocols = ProtocolUtils.getProtocolDescendants(dataSet.getProtocolUsed());
					List<ObservableFeature> categoricalFeatures = new ArrayList<ObservableFeature>();
					for (Protocol protocol : protocols)
					{
						List<ObservableFeature> observableFeatures = protocol.getFeatures();
						if (observableFeatures != null)
						{
							for (ObservableFeature observableFeature : observableFeatures)
							{
								String dataType = observableFeature.getDataType();
								FieldType type = MolgenisFieldTypes.getType(dataType);
								if (type.getEnumType() == FieldTypeEnum.CATEGORICAL)
								{
									categoricalFeatures.add(observableFeature);
								}
							}
						}
					}
					for (ObservableFeature categoricalFeature : categoricalFeatures)
					{
						if (!dataService.hasRepository(OmxLookupTableEntityMetaData
								.createOmxLookupTableEntityMetaDataName(categoricalFeature.getIdentifier())))
						{
							dataService.addRepository(new OmxLookupTableRepository(dataService, categoricalFeature
									.getIdentifier(), queryResolver));
							newRepoIdentifiers.add(OmxLookupTableEntityMetaData
									.createOmxLookupTableEntityMetaDataName(categoricalFeature.getIdentifier()));
						}
					}
				}

				// Check if all column names in the excel sheet exist as attributes of the entity
				Set<ConstraintViolation> violations = Sets.newLinkedHashSet();
				EntityMetaData meta = dataService.getEntityMetaData(identifier);
				for (AttributeMetaData attr : repository.getEntityMetaData().getAttributes())
				{
					if (meta.getAttribute(attr.getName()) == null)
					{
						String message = String.format("Unknown attributename '%s' for entity '%s'. Sheet: '%s'",
								attr.getName(), meta.getName(), repository.getName());
						violations.add(new ConstraintViolation(message, attr.getName(), null, null, meta, 0));
					}
				}

				if (!violations.isEmpty())
				{
					throw new MolgenisValidationException(violations);
				}

				// Import data into new OmxRepository
				try
				{
					dataService.add(identifier, repository);
				}
				catch (MolgenisValidationException e)
				{
					// Add sheet info
					for (ConstraintViolation violation : e.getViolations())
					{
						if (violation.getRownr() > 0)
						{

							// Rownr +1 for header
							violation.setImportInfo(String.format("Sheet: '%s', row: %d", repository.getName(),
									violation.getRownr() + 1));
						}
						else
						{
							violation.setImportInfo(String.format("Sheet: '%s'", repository.getName()));
						}

					}

					for (String newRepoIdentifier : newRepoIdentifiers)
					{
						dataService.removeRepository(newRepoIdentifier);
					}

					throw e;
				}

				int count = (int) RepositoryUtils.count(repository);
				importReport.addEntityCount(identifier, count);
				importReport.addNrImported(count);
			}
		}

		return importReport;
	}

	// RULE: Feature can only belong to one Protocol in a DataSet.(see issue #1136)
	private void checkFeatureCanOnlyBelongToOneProtocolForOneDataSet()
	{
		// RULE: Feature can only belong to one Protocol in a DataSet. Check it (see issue #1136)
		Iterable<DataSet> dataSets = dataService.findAll(DataSet.ENTITY_NAME, DataSet.class);
		for (DataSet dataSet : dataSets)
		{
			List<Protocol> dataSetProtocols = ProtocolUtils.getProtocolDescendants(dataSet.getProtocolUsed(), true);

			for (Protocol protocol : dataSetProtocols)
			{
				for (ObservableFeature feature : protocol.getFeatures())
				{
					for (Protocol p : dataSetProtocols)
					{
						if (!p.equals(protocol) && p.getFeatures().contains(feature))
						{
							String message = String
									.format("An ObservableFeature can only belong to one Protocol but feature '%s' belongs to both '%s' and '%s'",
											feature.getIdentifier(), p.getIdentifier(), protocol.getIdentifier());

							throw new MolgenisValidationException(Sets.newHashSet(new ConstraintViolation(message,
									feature.getIdentifier(), feature, null, null, 0)));
						}
					}
				}
			}
		}

	}
}
