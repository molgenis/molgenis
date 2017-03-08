package org.molgenis.ontology.importer;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.importer.EntitiesValidationReport;
import org.molgenis.data.importer.EntitiesValidationReportImpl;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY;

@Service
public class OntologyImportService implements ImportService
{
	private final DataService dataService;
	private final SearchService searchService;
	private final PermissionSystemService permissionSystemService;

	@Autowired
	public OntologyImportService(FileRepositoryCollectionFactory fileRepositoryCollectionFactory,
			DataService dataService, SearchService searchService, PermissionSystemService permissionSystemService)
	{
		this.dataService = requireNonNull(dataService);
		this.searchService = requireNonNull(searchService);
		this.permissionSystemService = requireNonNull(permissionSystemService);
	}

	@Override
	@Transactional
	public EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction,
			String defaultPackage)
	{
		if (databaseAction != DatabaseAction.ADD) throw new IllegalArgumentException("Only ADD is supported");

		List<EntityType> addedEntities = Lists.newArrayList();
		EntityImportReport report = new EntityImportReport();
		try
		{
			Iterator<String> it = source.getEntityIds().iterator();
			while (it.hasNext())
			{
				String entityNameToImport = it.next();
				Repository<Entity> repo = source.getRepository(entityNameToImport);
				try
				{
					report = new EntityImportReport();

					Repository<Entity> crudRepository = dataService.getRepository(entityNameToImport);

					crudRepository.add(stream(repo.spliterator(), false));

					List<String> entityNames = addedEntities.stream().map(emd -> emd.getFullyQualifiedName())
							.collect(Collectors.toList());
					permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(), entityNames);
					int count = 1;
					for (String entityName : entityNames)
					{
						report.addEntityCount(entityName, count++);
					}
				}
				finally
				{
					IOUtils.closeQuietly(repo);
				}
			}
		}
		catch (Exception e)
		{
			// Remove created repositories
			for (EntityType emd : addedEntities)
			{
				if (dataService.hasRepository(emd.getFullyQualifiedName()))
				{
					dataService.deleteAll(emd.getFullyQualifiedName());
				}

				if (searchService.hasMapping(emd))
				{
					searchService.delete(emd);
				}
			}

			throw new MolgenisDataException(e);
		}

		return report;
	}

	@Override
	/**
	 * Ontology validation
	 */ public EntitiesValidationReport validateImport(File file, RepositoryCollection source)
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
					new QueryImpl<Entity>().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyIRI).or()
							.eq(OntologyMetaData.ONTOLOGY_NAME, ontologyName));
			ontologyExists = ontologyQueryEntity != null;
		}

		if (ontologyExists) throw new MolgenisDataException("The ontology you are trying to import already exists");

		Iterator<String> it = source.getEntityIds().iterator();
		while (it.hasNext())
		{
			String entityName = it.next();
			report.getSheetsImportable().put(entityName, !ontologyExists);
		}
		return report;
	}

	@Override
	public boolean canImport(File file, RepositoryCollection source)
	{
		for (String extension : GenericImporterExtensions.getOntology())
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
		return GenericImporterExtensions.getOntology();
	}

	@Override
	public LinkedHashMap<String, Boolean> determineImportableEntities(MetaDataService metaDataService,
			RepositoryCollection repositoryCollection, String defaultPackage)
	{
		return metaDataService.determineImportableEntities(repositoryCollection);
	}
}
