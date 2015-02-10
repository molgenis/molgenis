package org.molgenis.data.vcf.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.elasticsearch.ElasticsearchRepository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.importer.EntitiesValidationReportImpl;
import org.molgenis.data.importer.ImportService;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class VcfImporterService implements ImportService
{
	private static final List<String> SUPPORTED_FILE_EXTENSIONS = Arrays.asList("vcf", "vcf.gz");
	private static final int DEFAULT_BATCH_SIZE = 1000;
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final DataService dataService;
	private final SearchService searchService;
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;
	private final PermissionSystemService permissionSystemService;

	@Autowired
	public VcfImporterService(FileRepositoryCollectionFactory fileRepositoryCollectionFactory, DataService dataService,
			SearchService searchService, RepositoryDecoratorFactory repositoryDecoratorFactory,
			PermissionSystemService permissionSystemService)
	{
		if (fileRepositoryCollectionFactory == null) throw new IllegalArgumentException(
				"fileRepositoryCollectionFactory is null");
		if (dataService == null) throw new IllegalArgumentException("dataservice is null");
		if (searchService == null) throw new IllegalArgumentException("seachservice is null");
		if (repositoryDecoratorFactory == null) throw new IllegalArgumentException("repositoryDecoratorFactory is null");
		if (permissionSystemService == null) throw new IllegalArgumentException("permissionSystemService is null");
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.dataService = dataService;
		this.searchService = searchService;
		this.repositoryDecoratorFactory = repositoryDecoratorFactory;
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
				Repository repo = source.getRepositoryByEntityName(it.next());
				try
				{
					report = importVcf(repo, DEFAULT_BATCH_SIZE, addedEntities);
					List<String> entityNames = addedEntities.stream().map(emd -> emd.getName())
							.collect(Collectors.toList());
					permissionSystemService.giveUserEntityAndMenuPermissions(SecurityContextHolder.getContext(),
							entityNames);
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
	public EntitiesValidationReport validateImport(File file, RepositoryCollection source)
	{
		EntitiesValidationReport report = new EntitiesValidationReportImpl();
		Iterator<String> it = source.getEntityNames().iterator();
		if (it.hasNext())
		{
			String entityName = it.next();
			EntityMetaData emd = source.getRepositoryByEntityName(entityName).getEntityMetaData();

			// Vcf entity
			boolean entityExists = dataService.hasRepository(entityName);
			report.getSheetsImportable().put(entityName, !entityExists);

			// Available Attributes
			List<String> availableAttributeNames = Lists.newArrayList();
			for (AttributeMetaData attr : emd.getAtomicAttributes())
			{
				availableAttributeNames.add(attr.getName());
			}
			report.getFieldsImportable().put(entityName, availableAttributeNames);

			// Sample entity
			AttributeMetaData sampleAttribute = emd.getAttribute("SAMPLES");
			if (sampleAttribute != null)
			{
				boolean sampleEntityExists = dataService.hasRepository(entityName);
				String sampleEntityName = sampleAttribute.getRefEntity().getName();
				report.getSheetsImportable().put(sampleEntityName, !sampleEntityExists);

				List<String> availableSampleAttributeNames = Lists.newArrayList();
				for (AttributeMetaData attr : sampleAttribute.getRefEntity().getAtomicAttributes())
				{
					availableSampleAttributeNames.add(attr.getName());
				}
				report.getFieldsImportable().put(sampleEntityName, availableSampleAttributeNames);
			}

		}

		return report;
	}

	@Override
	public boolean canImport(File file, RepositoryCollection source)
	{
		for (String extension : SUPPORTED_FILE_EXTENSIONS)
		{
			if (file.getName().toLowerCase().endsWith(extension))
			{
				return true;
			}
		}

		return false;
	}

	public void importVcf(File vcfFile) throws IOException
	{
		RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
				.createFileRepositoryCollection(vcfFile);

		Iterator<String> it = repositoryCollection.getEntityNames().iterator();
		if (it.hasNext())
		{
			Repository repo = repositoryCollection.getRepositoryByEntityName(it.next());
			try
			{
				importVcf(repo, DEFAULT_BATCH_SIZE, Lists.<EntityMetaData> newArrayList());
			}
			finally
			{
				IOUtils.closeQuietly(repo);
			}
		}
	}

	public EntityImportReport importVcf(Repository inRepository, int batchSize, List<EntityMetaData> addedEntities)
			throws IOException
	{
		EntityImportReport report = new EntityImportReport();
		ElasticsearchRepository sampleRepository = null;
		String entityName = inRepository.getName();

		if (dataService.hasRepository(entityName))
		{
			throw new MolgenisDataException("Can't overwrite existing " + entityName);
		}

		EntityMetaData entityMetaData = inRepository.getEntityMetaData();
		ElasticsearchRepository outRepository = new ElasticsearchRepository(entityMetaData, searchService);
		searchService.createMappings(entityMetaData, true, true, true, true);
		addedEntities.add(entityMetaData);

		AttributeMetaData sampleAttribute = entityMetaData.getAttribute("SAMPLES");
		if (sampleAttribute != null)
		{
			sampleRepository = new ElasticsearchRepository(sampleAttribute.getRefEntity(), searchService);
			searchService.createMappings(sampleAttribute.getRefEntity(), true, true, true, true);
			addedEntities.add(sampleAttribute.getRefEntity());
		}

		Iterator<Entity> inIterator = inRepository.iterator();
		int vcfEntityCount = 0;
		int sampleEntityCount = 0;
		try
		{
			List<Entity> sampleEntities = new ArrayList<Entity>();

			while (inIterator.hasNext())
			{
				Entity entity = inIterator.next();
				vcfEntityCount++;

				if (sampleRepository != null)
				{
					Iterable<Entity> samples = entity.getEntities("SAMPLES");
					if (samples != null)
					{
						Iterator<Entity> sampleIterator = samples.iterator();
						while (sampleIterator.hasNext())
						{
							sampleEntities.add(sampleIterator.next());

							if (sampleEntities.size() == batchSize)
							{
								sampleRepository.add(sampleEntities);
								sampleEntityCount += sampleEntities.size();
								sampleEntities.clear();
							}
						}
					}
				}
			}

			outRepository.add(inRepository);

			if (sampleRepository != null)
			{
				sampleRepository.add(sampleEntities);
				sampleEntityCount += sampleEntities.size();
			}
		}
		finally
		{
			outRepository.close();
		}

		dataService.addRepository(repositoryDecoratorFactory.createDecoratedRepository(outRepository));
		report.addNewEntity(entityName);

		if (sampleRepository != null)
		{
			dataService.addRepository(sampleRepository);
			report.addNewEntity(sampleRepository.getName());
			if (sampleEntityCount > 0) report.addEntityCount(sampleRepository.getName(), sampleEntityCount);
		}

		if (vcfEntityCount > 0)
		{
			report.addEntityCount(entityName, vcfEntityCount);
		}

		return report;
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
		return true;
	}

}
