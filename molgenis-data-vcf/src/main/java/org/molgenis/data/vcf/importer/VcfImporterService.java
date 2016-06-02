package org.molgenis.data.vcf.importer;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.EntitiesValidationReportImpl;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.data.vcf.VcfAttributes;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.permission.PermissionSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class VcfImporterService implements ImportService
{
	private static final Logger LOG = LoggerFactory.getLogger(VcfImporterService.class);
	private static final int BATCH_SIZE = 10000;

	private final DataService dataService;
	private final PermissionSystemService permissionSystemService;

	@Autowired
	public VcfImporterService(DataService dataService, PermissionSystemService permissionSystemService)

	{
		this.dataService = requireNonNull(dataService);
		this.permissionSystemService = requireNonNull(permissionSystemService);
	}

	@Override
	public EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction,
			String defaultPackage)
	{
		if (databaseAction != DatabaseAction.ADD) throw new IllegalArgumentException("Only ADD is supported");

		List<EntityMetaData> addedEntities = Lists.newArrayList();
		EntityImportReport report;
		try
		{
			Iterator<String> it = source.getEntityNames().iterator();
			if (it.hasNext())
			{
				try (Repository<Entity> repo = source.getRepository(it.next());)
				{
					report = importVcf(repo, addedEntities);
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
			AttributeMetaData sampleAttribute = emd.getAttribute(VcfAttributes.SAMPLES);
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

	private EntityImportReport importVcf(Repository<Entity> inRepository, List<EntityMetaData> addedEntities) throws IOException
	{
		EntityImportReport report = new EntityImportReport();
		Repository<Entity> sampleRepository = null;
		String entityName = inRepository.getName();

		if (dataService.hasRepository(entityName))
		{
			throw new MolgenisDataException("Can't overwrite existing " + entityName);
		}

		EntityMetaData entityMetaData = new EntityMetaDataImpl(inRepository.getEntityMetaData());

		AttributeMetaData sampleAttribute = entityMetaData.getAttribute(VcfAttributes.SAMPLES);
		if (sampleAttribute != null)
		{
			EntityMetaData samplesEntityMetaData = new EntityMetaDataImpl(sampleAttribute.getRefEntity());
			sampleRepository = dataService.getMeta().addEntityMeta(samplesEntityMetaData);
			permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
					Collections.singletonList(samplesEntityMetaData.getName()));
			addedEntities.add(sampleAttribute.getRefEntity());
		}

		Iterator<Entity> inIterator = inRepository.iterator();
		int sampleEntityCount = 0;
		List<Entity> sampleEntities = new ArrayList<>();
		try (Repository<Entity> outRepository = dataService.getMeta().addEntityMeta(entityMetaData))
		{
			permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
					Collections.singletonList(entityMetaData.getName()));

			addedEntities.add(entityMetaData);

			if (sampleRepository != null)
			{
				while (inIterator.hasNext())
				{
					Entity entity = inIterator.next();

					Iterable<Entity> samples = entity.getEntities(VcfAttributes.SAMPLES);
					if (samples != null)
					{
						Iterator<Entity> sampleIterator = samples.iterator();
						while (sampleIterator.hasNext())
						{
							sampleEntities.add(sampleIterator.next());

							if (sampleEntities.size() == BATCH_SIZE)
							{
								sampleRepository.add(sampleEntities.stream());
								sampleEntityCount += sampleEntities.size();
								sampleEntities.clear();
							}
						}
					}

				}

				if (!sampleEntities.isEmpty())
				{
					sampleRepository.add(sampleEntities.stream());
					sampleEntityCount += sampleEntities.size();
				}

				sampleRepository.flush();

				report.addNewEntity(sampleRepository.getName());
				if (sampleEntityCount > 0)
				{
					report.addEntityCount(sampleRepository.getName(), sampleEntityCount);
				}
			}

			AtomicInteger vcfEntityCount = new AtomicInteger();
			outRepository.add(inRepository.stream().filter(entity -> {
				vcfEntityCount.incrementAndGet();
				return true;
			}));
			if (vcfEntityCount.get() > 0)
			{
				report.addEntityCount(entityName, vcfEntityCount.get());
			}
		}

		report.addNewEntity(entityName);

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

	@Override
	public LinkedHashMap<String, Boolean> integrationTestMetaData(MetaDataService metaDataService,
			RepositoryCollection repositoryCollection, String defaultPackage)
	{
		return metaDataService.integrationTestMetaData(repositoryCollection);
	}
}
