package org.molgenis.data.vcf.importer;

import com.google.common.collect.Lists;
import org.molgenis.data.*;
import org.molgenis.data.importer.EntitiesValidationReport;
import org.molgenis.data.importer.EntitiesValidationReportImpl;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.vcf.VcfFileExtensions;
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
	private final DefaultPackage defaultPackage;

	@Autowired
	public VcfImporterService(DataService dataService, PermissionSystemService permissionSystemService,
			MetaDataService metaDataService, DefaultPackage defaultPackage)

	{
		this.dataService = requireNonNull(dataService);
		this.metaDataService = requireNonNull(metaDataService);
		this.permissionSystemService = requireNonNull(permissionSystemService);
		this.defaultPackage = requireNonNull(defaultPackage);
	}

	@Transactional
	@Override
	public EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction, String packageId)
	{
		if (databaseAction != DatabaseAction.ADD) throw new IllegalArgumentException("Only ADD is supported");

		List<EntityType> addedEntities = Lists.newArrayList();
		EntityImportReport report;

		Iterator<String> it = source.getEntityTypeIds().iterator();
		if (it.hasNext())
		{
			try (Repository<Entity> repo = source.getRepository(it.next()))
			{
				report = importVcf(repo, addedEntities, packageId);
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
		Iterator<String> it = source.getEntityTypeIds().iterator();
		if (it.hasNext())
		{
			String entityTypeId = it.next();
			EntityType emd = source.getRepository(entityTypeId).getEntityType();

			// Vcf entity
			boolean entityExists = runAsSystem(() -> dataService.hasRepository(entityTypeId));
			report.getSheetsImportable().put(entityTypeId, !entityExists);

			// Available Attributes
			List<String> availableAttributeNames = Lists.newArrayList();
			for (Attribute attr : emd.getAtomicAttributes())
			{
				availableAttributeNames.add(attr.getName());
			}
			report.getFieldsImportable().put(entityTypeId, availableAttributeNames);

			// Sample entity
			Attribute sampleAttribute = emd.getAttribute(VcfAttributes.SAMPLES);
			if (sampleAttribute != null)
			{
				String sampleEntityName = sampleAttribute.getRefEntity().getId();
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
		for (String extension : getSupportedFileExtensions())
		{
			if (file.getName().toLowerCase().endsWith(extension))
			{
				return true;
			}
		}

		return false;
	}

	private EntityImportReport importVcf(Repository<Entity> inRepository, List<EntityType> addedEntities,
			String packageId) throws IOException
	{
		EntityImportReport report = new EntityImportReport();
		Repository<Entity> sampleRepository;
		String entityTypeId = inRepository.getName();

		if (runAsSystem(() -> dataService.hasRepository(entityTypeId)))
		{
			throw new MolgenisDataException("Can't overwrite existing " + entityTypeId);
		}

		EntityType entityType = inRepository.getEntityType();
		entityType.setBackend(metaDataService.getDefaultBackend().getName());

		Package package_ = dataService.getMeta().getPackage(packageId);
		if (package_ == null) package_ = defaultPackage;
		entityType.setPackage(package_);

		Attribute sampleAttribute = entityType.getAttribute(VcfAttributes.SAMPLES);
		if (sampleAttribute != null)
		{
			EntityType samplesEntityType = sampleAttribute.getRefEntity();
			samplesEntityType.setBackend(metaDataService.getDefaultBackend().getName());
			samplesEntityType.setPackage(package_);
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
						for (Entity sample : samples)
						{
							sampleEntities.add(sample);

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
				report.addEntityCount(entityTypeId, vcfEntityCount.get());
			}
		}

		report.addNewEntity(entityTypeId);

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
		return VcfFileExtensions.getVCF();
	}

	@Override
	public LinkedHashMap<String, Boolean> determineImportableEntities(MetaDataService metaDataService,
			RepositoryCollection repositoryCollection, String defaultPackage)
	{
		return metaDataService.determineImportableEntities(repositoryCollection);
	}
}
