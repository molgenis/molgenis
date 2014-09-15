package org.molgenis.data.omx.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepositorySecurityDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.jpa.importer.EntitiesImporter;
import org.molgenis.data.omx.OmxRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.QueryResolver;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntitiesValidator;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.protocol.OmxLookupTableEntityMetaData;
import org.molgenis.omx.protocol.OmxLookupTableRepository;
import org.molgenis.omx.utils.ProtocolUtils;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.EntityImportedEvent;
import org.molgenis.util.RepositoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

@Service
public class OmxImporterServiceImpl implements ImportService
{
	public static final String DATASET_SHEET_PREFIX = "dataset_";

	private final DataService dataService;
	private final SearchService searchService;
	private final EntitiesImporter entitiesImporter;
	private final EntityValidator entityValidator;
	private final EntitiesValidator entitiesValidator;
	private final QueryResolver queryResolver;

	@Autowired
	public OmxImporterServiceImpl(DataService dataService, SearchService searchService,
			EntitiesImporter entitiesImporter, EntityValidator entityValidator, QueryResolver queryResolver,
			EntitiesValidator entitiesValidator)
	{
		this.dataService = dataService;
		this.searchService = searchService;
		this.entitiesImporter = entitiesImporter;
		this.entityValidator = entityValidator;
		this.queryResolver = queryResolver;
		this.entitiesValidator = entitiesValidator;
	}

	@Override
	public EntitiesValidationReport validateImport(File file, RepositoryCollection source)
	{
		try
		{
			EntitiesValidationReport validationReport = entitiesValidator.validate(file);

			// remove data sheets
			Map<String, Boolean> entitiesImportable = validationReport.getSheetsImportable();
			if (entitiesImportable != null)
			{
				for (Iterator<Entry<String, Boolean>> it = entitiesImportable.entrySet().iterator(); it.hasNext();)
				{
					if (it.next().getKey().toLowerCase().startsWith("dataset_"))
					{
						it.remove();
					}
				}
			}

			return validationReport;
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	@Override
	public boolean canImport(File file, RepositoryCollection source)
	{
		// Use this as fallback
		return true;
	}

	@Override
	@Transactional
	public EntityImportReport doImport(RepositoryCollection repositories, DatabaseAction databaseAction)
	{
		// All new repository identifiers
		List<String> newRepoIdentifiers = new ArrayList<String>();

		// First import entities, the data sheets are ignored in the entitiesimporter
		EntityImportReport importReport;
		try
		{
			importReport = entitiesImporter.importEntities(repositories, databaseAction);
		}
		catch (IOException e1)
		{
			throw new MolgenisDataException(e1);
		}

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
					dataService.addRepository(new CrudRepositorySecurityDecorator(new OmxRepository(dataService,
							searchService, identifier, entityValidator)));
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
			}
		}

		// publish dataset imported event(s) if we are in a spring environment
		if (ApplicationContextProvider.getApplicationContext() != null)
		{
			Iterable<String> entities = repositories.getEntityNames();
			for (String entityName : entities)
			{
				if (entityName.startsWith(DATASET_SHEET_PREFIX))
				{
					String dataSetIdentifier = entityName.substring(DATASET_SHEET_PREFIX.length());
					DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
							new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier), DataSet.class);
					ApplicationContextProvider.getApplicationContext().publishEvent(
							new EntityImportedEvent(this, DataSet.ENTITY_NAME, dataSet.getId()));
				}
				if (Protocol.ENTITY_NAME.equalsIgnoreCase(entityName))
				{
					Repository repo = repositories.getRepositoryByEntityName("protocol");

					for (Protocol protocol : repo.iterator(Protocol.class))
					{
						if (protocol.getRoot())
						{
							Protocol rootProtocol = dataService.findOne(Protocol.ENTITY_NAME,
									new QueryImpl().eq(Protocol.IDENTIFIER, protocol.getIdentifier()), Protocol.class);
							ApplicationContextProvider.getApplicationContext().publishEvent(
									new EntityImportedEvent(this, Protocol.ENTITY_NAME, rootProtocol.getId()));
						}
					}
				}
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

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}

}
