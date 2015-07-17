package org.molgenis.ontology.importer;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.importer.EntitiesValidationReportImpl;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.file.FileStore;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Service
public class OntologyImportService implements ImportService
{
	private final DataService dataService;
	private final SearchService searchService;
	private final PermissionSystemService permissionSystemService;

	@Autowired
	private FileStore fileStore;

	@Autowired
	public OntologyImportService(FileRepositoryCollectionFactory fileRepositoryCollectionFactory,
			DataService dataService, SearchService searchService, PermissionSystemService permissionSystemService)
	{
		if (fileRepositoryCollectionFactory == null) throw new IllegalArgumentException(
				"fileRepositoryCollectionFactory is null");
		if (dataService == null) throw new IllegalArgumentException("dataservice is null");
		if (searchService == null) throw new IllegalArgumentException("seachservice is null");
		if (permissionSystemService == null) throw new IllegalArgumentException("permissionSystemService is null");
		this.dataService = dataService;
		this.searchService = searchService;
		this.permissionSystemService = permissionSystemService;
	}

	@Override
	@Transactional
	public EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction, String defaultPackage)
	{
		if (databaseAction != DatabaseAction.ADD) throw new IllegalArgumentException("Only ADD is supported");

		List<EntityMetaData> addedEntities = Lists.newArrayList();
		EntityImportReport report = new EntityImportReport();
		try
		{
			Iterator<String> it = source.getEntityNames().iterator();
			while (it.hasNext())
			{
				String entityNameToImport = it.next();
				Repository repo = source.getRepository(entityNameToImport);
				try
				{
					report = new EntityImportReport();

					Repository crudRepository = dataService.getRepository(entityNameToImport);

					crudRepository.add(repo);

					List<String> entityNames = addedEntities.stream().map(emd -> emd.getName())
							.collect(Collectors.toList());
					permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
							entityNames);
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
			for (EntityMetaData emd : addedEntities)
			{
				if (dataService.hasRepository(emd.getName()))
				{
					dataService.deleteAll(emd.getName());
				}

				if (searchService.hasMapping(emd))
				{
					searchService.delete(emd.getName());
				}
			}

			throw new MolgenisDataException(e);
		}

		return report;
	}

	@Override
	/**
	 * Ontology validation 
	 */
	public EntitiesValidationReport validateImport(File file, RepositoryCollection source)
	{
		EntitiesValidationReport report = new EntitiesValidationReportImpl();

		if (source.getRepository(OntologyMetaData.ENTITY_NAME) == null) throw new MolgenisDataException(
				"Exception Repository [" + OntologyMetaData.ENTITY_NAME + "] is missing");

		boolean ontologyExists = false;
		for (Entity ontologyEntity : source.getRepository(OntologyMetaData.ENTITY_NAME))
		{
			String ontologyIRI = ontologyEntity.getString(OntologyMetaData.ONTOLOGY_IRI);
			String ontologyName = ontologyEntity.getString(OntologyMetaData.ONTOLOGY_NAME);

			Entity ontologyQueryEntity = dataService.findOne(
					OntologyMetaData.ENTITY_NAME,
					new QueryImpl().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyIRI).or()
							.eq(OntologyMetaData.ONTOLOGY_NAME, ontologyName));
			ontologyExists = ontologyQueryEntity != null;
		}

		Iterator<String> it = source.getEntityNames().iterator();
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
}
