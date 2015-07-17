package org.molgenis.data.vcf.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.data.importer.EntitiesValidationReportImpl;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.permission.PermissionSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Service
public class VcfImporterService implements ImportService
{
	private static final Logger LOG = LoggerFactory.getLogger(VcfImporterService.class);
	private static final int BATCH_SIZE = 10000;
	private static final String BACKEND = ElasticsearchRepositoryCollection.NAME;
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final DataService dataService;
	private final PermissionSystemService permissionSystemService;

	@Autowired
	public VcfImporterService(FileRepositoryCollectionFactory fileRepositoryCollectionFactory, DataService dataService,
			PermissionSystemService permissionSystemService)

	{
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.dataService = dataService;
		this.permissionSystemService = permissionSystemService;
	}

	@Override
	@Transactional
	public EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction, String defaultPackage)
	{
		if (databaseAction != DatabaseAction.ADD) throw new IllegalArgumentException("Only ADD is supported");

		List<EntityMetaData> addedEntities = Lists.newArrayList();
		EntityImportReport report;
		try
		{
			Iterator<String> it = source.getEntityNames().iterator();
			if (it.hasNext())
			{
				try (Repository repo = source.getRepository(it.next());)
				{
					report = importVcf(repo, addedEntities);
					List<String> entityNames = addedEntities.stream().map(emd -> emd.getName())
							.collect(Collectors.toList());
					permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
							entityNames);
				}
			}
			else
			{
				report = new EntityImportReport();
			}
		}
		catch (Exception e)
		{
			LOG.error("Exception importing vcf", e);

			// Remove created repositories
			try
			{
				dataService.getMeta().delete(addedEntities);
			}
			catch (Exception e1)
			{
				LOG.error("Exception rollback changes", e1);
			}

			throw new MolgenisDataException(e);
		}
		// Should not be necessary, bug in elasticsearch?
		// "All shards failed" for big datasets if this flush is not here...
		for (EntityMetaData entityMetaData : addedEntities)
		{
			dataService.getRepository(entityMetaData.getName()).flush();
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
			EntityMetaData emd = source.getRepository(entityName).getEntityMetaData();

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
			AttributeMetaData sampleAttribute = emd.getAttribute(VcfRepository.SAMPLES);
			if (sampleAttribute != null)
			{
				String sampleEntityName = sampleAttribute.getRefEntity().getName();
				boolean sampleEntityExists = dataService.hasRepository(sampleEntityName);
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
		for (String extension : GenericImporterExtensions.getVCF())
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
			Repository repo = repositoryCollection.getRepository(it.next());
			try
			{
				importVcf(repo, Lists.<EntityMetaData> newArrayList());
			}
			finally
			{
				IOUtils.closeQuietly(repo);
			}
		}
	}

	private EntityImportReport importVcf(Repository inRepository, List<EntityMetaData> addedEntities)
			throws IOException
	{
		EntityImportReport report = new EntityImportReport();
		Repository sampleRepository = null;
		String entityName = inRepository.getName();

		if (dataService.hasRepository(entityName))
		{
			throw new MolgenisDataException("Can't overwrite existing " + entityName);
		}

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(inRepository.getEntityMetaData());
		entityMetaData.setBackend(BACKEND);

		AttributeMetaData sampleAttribute = entityMetaData.getAttribute(VcfRepository.SAMPLES);
		if (sampleAttribute != null)
		{
			DefaultEntityMetaData samplesEntityMetaData = new DefaultEntityMetaData(sampleAttribute.getRefEntity());
			samplesEntityMetaData.setBackend(BACKEND);
			sampleRepository = dataService.getMeta().addEntityMeta(samplesEntityMetaData);
			permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
					Collections.singletonList(samplesEntityMetaData.getName()));
			addedEntities.add(sampleAttribute.getRefEntity());
		}

		Iterator<Entity> inIterator = inRepository.iterator();
		int vcfEntityCount = 0;
		int sampleEntityCount = 0;
		List<Entity> sampleEntities = new ArrayList<>();
		try (Repository outRepository = dataService.getMeta().addEntityMeta(entityMetaData))
		{
			permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
					Collections.singletonList(entityMetaData.getName()));

			addedEntities.add(entityMetaData);

			if (sampleRepository != null)
			{
				while (inIterator.hasNext())
				{
					Entity entity = inIterator.next();
					vcfEntityCount++;

					Iterable<Entity> samples = entity.getEntities(VcfRepository.SAMPLES);
					if (samples != null)
					{
						Iterator<Entity> sampleIterator = samples.iterator();
						while (sampleIterator.hasNext())
						{
							sampleEntities.add(sampleIterator.next());

							if (sampleEntities.size() == BATCH_SIZE)
							{
								sampleRepository.add(sampleEntities);
								sampleEntityCount += sampleEntities.size();
								sampleEntities.clear();
							}
						}
					}

				}

				if (!sampleEntities.isEmpty())
				{
					sampleRepository.add(sampleEntities);
					sampleEntityCount += sampleEntities.size();
				}

				sampleRepository.flush();

				report.addNewEntity(sampleRepository.getName());
				if (sampleEntityCount > 0) report.addEntityCount(sampleRepository.getName(), sampleEntityCount);
			}

			outRepository.add(inRepository);
		}

		report.addNewEntity(entityName);

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

	@Override
	public Set<String> getSupportedFileExtensions()
	{
		return GenericImporterExtensions.getVCF();
	}
}
