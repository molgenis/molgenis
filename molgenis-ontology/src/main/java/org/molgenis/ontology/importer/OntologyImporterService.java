package org.molgenis.ontology.importer;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.importer.EntitiesValidationReportImpl;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.ontology.OntologyRepository;
import org.molgenis.ontology.OntologyRepositoryCollection;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.index.OntologyIndexer;
import org.molgenis.ontology.repository.OntologyQueryRepository;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class OntologyImporterService implements ImportService
{
	private final DataService dataService;
	private final SearchService searchService;
	private final PermissionSystemService permissionSystemService;

	@Autowired
	private FileStore fileStore;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private OntologyIndexer ontologyIndexer;

	@Autowired
	public OntologyImporterService(FileRepositoryCollectionFactory fileRepositoryCollectionFactory,
			DataService dataService, SearchService searchService, PermissionSystemService permissionSystemService)
	{
		System.out.println("OntologyImporterService");

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
	public EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction)
	{
		if (databaseAction != DatabaseAction.ADD) throw new IllegalArgumentException("Only ADD is supported");

		List<EntityMetaData> addedEntities = Lists.newArrayList();
		EntityImportReport report;
		try
		{
			Iterator<String> it = source.getEntityNames().iterator();
			if (it.hasNext())
			{
				OntologyRepository repo = (OntologyRepository) source.getRepositoryByEntityName(it.next());
				try
				{
					report = new EntityImportReport();
					ontologyIndexer.index(repo.getOntologyLoader());
					List<String> entityNames = addedEntities.stream().map(emd -> emd.getName())
							.collect(Collectors.toList());
					permissionSystemService.giveUserEntityAndMenuPermissions(SecurityContextHolder.getContext(),
							entityNames);
					int count = 1;
					for(String entityName: entityNames)
					{
						report.addEntityCount(entityName, count++);
					}
				}
				finally
				{
					IOUtils.closeQuietly(repo);
				}
			}
			else
			{
				report = new EntityImportReport();
			}
		}
		catch (Exception e)
		{
			// Remove created repositories
			for (EntityMetaData emd : addedEntities)
			{
				if (dataService.hasRepository(emd.getName()))
				{
					dataService.removeRepository(emd.getName());
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
		Iterator<String> it = source.getEntityNames().iterator();
		if (it.hasNext())
		{
			String entityName = it.next();
			boolean entityExists = dataService.hasRepository(entityName);

			// Check if ontology IRI exists
			String ontologyIRI = ((OntologyRepository) source.getRepositoryByEntityName(entityName))
					.getOntologyLoader().getOntologyIRI();

			Entity ontologyQueryEntity = dataService.findOne(OntologyQueryRepository.ENTITY_NAME,
					new QueryImpl().eq(OntologyQueryRepository.ONTOLOGY_IRI, ontologyIRI));

			boolean ontologyQueryEntityExists = ontologyQueryEntity != null;

			report.getSheetsImportable().put(entityName, !entityExists && !ontologyQueryEntityExists);
		}

		return report;
	}

	@Override
	public boolean canImport(File file, RepositoryCollection source)
	{
		for (String extension : OntologyRepositoryCollection.EXTENSIONS)
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

}
