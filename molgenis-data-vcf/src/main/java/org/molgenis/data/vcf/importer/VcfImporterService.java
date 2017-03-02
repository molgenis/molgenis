package org.molgenis.data.vcf.importer;

import com.google.common.collect.Lists;
import org.molgenis.data.*;
import org.molgenis.data.importer.EntitiesValidationReport;
import org.molgenis.data.importer.EntitiesValidationReportImpl;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.security.permission.PermissionSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

@Service
public class VcfImporterService implements ImportService
{
	private static final Logger LOG = LoggerFactory.getLogger(VcfImporterService.class);
	private static final int BATCH_SIZE = 10000;

	private final DataService dataService;
	private final PermissionSystemService permissionSystemService;
	private final MetaDataService metaDataService;

	@Autowired
	public VcfImporterService(DataService dataService, PermissionSystemService permissionSystemService,
			MetaDataService metaDataService)

	{
		this.dataService = requireNonNull(dataService);
		this.metaDataService = requireNonNull(metaDataService);
		this.permissionSystemService = requireNonNull(permissionSystemService);
	}

	@Transactional
	@Override
	public EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction,
			String defaultPackage)
	{
		if (databaseAction != DatabaseAction.ADD) throw new IllegalArgumentException("Only ADD is supported");

		List<EntityType> addedEntities = Lists.newArrayList();
		EntityImportReport report;

		Iterator<String> it = source.getEntityIds().iterator();
		if (it.hasNext())
		{
			try (Repository<Entity> repo = source.getRepository(it.next()))
			{
				report = importVcf(repo, addedEntities);
			}
			catch (IOException e)
			{
				LOG.error("", e);
				throw new MolgenisDataException(e);
			}
		}
		else
		{
			report = new EntityImportReport();
		}
		return report;
	}

	@Override
	public EntitiesValidationReport validateImport(File file, RepositoryCollection source)
	{
		EntitiesValidationReport report = new EntitiesValidationReportImpl();
		Iterator<String> it = source.getEntityIds().iterator();
		if (it.hasNext())
		{
			String entityName = it.next();
			EntityType emd = source.getRepository(entityName).getEntityType();

			// Vcf entity
			boolean entityExists = runAsSystem(() -> dataService.hasRepository(entityName));
			report.getSheetsImportable().put(entityName, !entityExists);

			// Available Attributes
			List<String> availableAttributeNames = Lists.newArrayList();
			for (Attribute attr : emd.getAtomicAttributes())
			{
				availableAttributeNames.add(attr.getName());
			}
			report.getFieldsImportable().put(entityName, availableAttributeNames);

			// Sample entity
			Attribute sampleAttribute = emd.getAttribute(VcfAttributes.SAMPLES);
			if (sampleAttribute != null)
			{
				String sampleEntityName = sampleAttribute.getRefEntity().getFullyQualifiedName();
				boolean sampleEntityExists = runAsSystem(() -> dataService.hasRepository(sampleEntityName));
				report.getSheetsImportable().put(sampleEntityName, !sampleEntityExists);

				List<String> availableSampleAttributeNames = Lists.newArrayList();
				for (Attribute attr : sampleAttribute.getRefEntity().getAtomicAttributes())
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

	private EntityImportReport importVcf(Repository<Entity> inRepository, List<EntityType> addedEntities)
			throws IOException
	{
		EntityImportReport report = new EntityImportReport();
		Repository<Entity> sampleRepository;
		String entityName = inRepository.getName();

		if (runAsSystem(() -> dataService.hasRepository(entityName)))
		{
			throw new MolgenisDataException("Can't overwrite existing " + entityName);
		}

		EntityType entityType = inRepository.getEntityType();
		entityType.setBackend(metaDataService.getDefaultBackend().getName());

		Attribute sampleAttribute = entityType.getAttribute(VcfAttributes.SAMPLES);
		if (sampleAttribute != null)
		{
			EntityType samplesEntityType = sampleAttribute.getRefEntity();
			samplesEntityType.setBackend(metaDataService.getDefaultBackend().getName());
			sampleRepository = runAsSystem(() -> dataService.getMeta().createRepository(samplesEntityType));
			permissionSystemService.giveUserWriteMetaPermissions(samplesEntityType);
			addedEntities.add(sampleAttribute.getRefEntity());
		}
		else
		{
			sampleRepository = null;
		}

		Iterator<Entity> inIterator = inRepository.iterator();
		int sampleEntityCount = 0;
		List<Entity> sampleEntities = new ArrayList<>();
		try (Repository<Entity> outRepository = runAsSystem(() -> dataService.getMeta().createRepository(entityType)))
		{
			permissionSystemService.giveUserWriteMetaPermissions(entityType);

			addedEntities.add(entityType);

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
					runAsSystem(() -> sampleRepository.add(sampleEntities.stream()));
					sampleEntityCount += sampleEntities.size();
				}

				report.addNewEntity(sampleRepository.getName());
				if (sampleEntityCount > 0)
				{
					report.addEntityCount(sampleRepository.getName(), sampleEntityCount);
				}
			}

			AtomicInteger vcfEntityCount = new AtomicInteger();
			runAsSystem(() -> outRepository.add(StreamSupport.stream(inRepository.spliterator(), false).filter(entity ->
			{
				vcfEntityCount.incrementAndGet();
				return true;
			})));
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
	public LinkedHashMap<String, Boolean> determineImportableEntities(MetaDataService metaDataService,
			RepositoryCollection repositoryCollection, String defaultPackage)
	{
		return metaDataService.determineImportableEntities(repositoryCollection);
	}
}
