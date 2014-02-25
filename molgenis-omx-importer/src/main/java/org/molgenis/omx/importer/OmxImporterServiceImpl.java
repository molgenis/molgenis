package org.molgenis.omx.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Repository;
import org.molgenis.data.omx.OmxLookupTableRepository;
import org.molgenis.data.omx.OmxRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.EntitiesImporter;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.utils.ProtocolUtils;
import org.molgenis.search.SearchService;
import org.molgenis.util.RepositoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

@Service
public class OmxImporterServiceImpl implements OmxImporterService
{
	@Autowired
	private DataService dataService;

	@Autowired
	private SearchService searchService;

	@Autowired
	private EntitiesImporter entitiesImporter;

	@Override
	@Transactional(rollbackFor = IOException.class)
	public EntityImportReport doImport(List<Repository> repositories, DatabaseAction databaseAction) throws IOException
	{
		// First import entities, the data sheets are ignored in the entitiesimporter
		EntityImportReport importReport = entitiesImporter.importEntities(repositories, databaseAction);

		// Import data sheets
		for (Repository repository : repositories)
		{
			if (repository.getName().startsWith(DATASET_SHEET_PREFIX))
			{
				// Import DataSet sheet
				String identifier = repository.getName().substring(DATASET_SHEET_PREFIX.length());
				if (!Iterables.contains(dataService.getEntityNames(), identifier))
				{
					dataService.addRepository(new OmxRepository(dataService, searchService, identifier));

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
						dataService.addRepository(new OmxLookupTableRepository(dataService, categoricalFeature
								.getIdentifier()));
					}

				}

				dataService.add(identifier, repository);
				int count = (int) RepositoryUtils.count(repository);
				importReport.addEntityCount(identifier, count);
				importReport.addNrImported(count);
			}
		}

		return importReport;
	}
}
