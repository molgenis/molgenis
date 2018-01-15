package org.molgenis.ontology.core.importer;

import com.google.common.collect.Lists;
import org.molgenis.data.*;
import org.molgenis.data.importer.EntitiesValidationReport;
import org.molgenis.data.importer.EntitiesValidationReportImpl;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.importer.repository.OntologyFileExtensions;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.util.EntityUtils.asStream;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY;

@Service
public class OntologyImportService implements ImportService
{
	private static final Logger LOG = LoggerFactory.getLogger(OntologyImportService.class);

	private final DataService dataService;

	public OntologyImportService(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	@Transactional
	public EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction, String packageId)
	{
		if (databaseAction != DatabaseAction.ADD)
		{
			throw new IllegalArgumentException("Only ADD is supported");
		}

		EntityImportReport report = new EntityImportReport();

		for (String entityTypeId : source.getEntityTypeIds())
		{
			try (Repository<Entity> sourceRepository = source.getRepository(entityTypeId))
			{
				Repository<Entity> targetRepository = dataService.getRepository(entityTypeId);
				Integer count = targetRepository.add(asStream(sourceRepository));
				report.addEntityCount(entityTypeId, count);
			}
			catch (IOException e)
			{
				LOG.error("", e);
				throw new MolgenisDataException(e);
			}
		}
		return report;
	}

	@Override
	public EntitiesValidationReport validateImport(File file, RepositoryCollection source)
	{
		EntitiesValidationReport report = new EntitiesValidationReportImpl();

		if (source.getRepository(ONTOLOGY) == null)
			throw new MolgenisDataException("Exception Repository [" + ONTOLOGY + "] is missing");

		boolean ontologyExists = false;
		for (Entity ontologyEntity : source.getRepository(ONTOLOGY))
		{
			String ontologyIRI = ontologyEntity.getString(OntologyMetaData.ONTOLOGY_IRI);
			String ontologyName = ontologyEntity.getString(OntologyMetaData.ONTOLOGY_NAME);

			Entity ontologyQueryEntity = dataService.findOne(ONTOLOGY,
					new QueryImpl<>().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyIRI)
									 .or()
									 .eq(OntologyMetaData.ONTOLOGY_NAME, ontologyName));
			ontologyExists = ontologyQueryEntity != null;
		}

		if (ontologyExists) throw new MolgenisDataException("The ontology you are trying to import already exists");

		for (String entityTypeId : source.getEntityTypeIds())
		{
			report.getSheetsImportable().put(entityTypeId, !ontologyExists);
		}
		return report;
	}

	@Override
	public boolean canImport(File file, RepositoryCollection source)
	{
		for (String extension : OntologyFileExtensions.getOntology())
		{
			if (file.getName().toLowerCase().endsWith(extension))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public int getOrder()
	{
		return 10;
	}

	@Override
	public List<DatabaseAction> getSupportedDatabaseActions()
	{
		return Lists.newArrayList(DatabaseAction.ADD);
	}

	@Override
	public boolean getMustChangeEntityName()
	{
		return false;
	}

	@Override
	public Set<String> getSupportedFileExtensions()
	{
		return OntologyFileExtensions.getOntology();
	}

	@Override
	public LinkedHashMap<String, Boolean> determineImportableEntities(MetaDataService metaDataService,
			RepositoryCollection repositoryCollection, String defaultPackage)
	{
		return metaDataService.determineImportableEntities(repositoryCollection);
	}
}
