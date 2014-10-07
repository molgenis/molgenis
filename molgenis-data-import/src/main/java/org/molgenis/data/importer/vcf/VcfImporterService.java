package org.molgenis.data.importer.vcf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.ElasticsearchRepository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.importer.EntitiesValidationReportImpl;
import org.molgenis.data.importer.ImportService;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VcfImporterService implements ImportService
{
	private static final List<String> SUPPORTED_FILE_EXTENSIONS = Arrays.asList("vcf", "vcf.gz");

	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final DataService dataService;
	private final SearchService searchService;

	@Autowired
	public VcfImporterService(FileRepositoryCollectionFactory fileRepositoryCollectionFactory, DataService dataService,
			SearchService searchService)
	{
		if (fileRepositoryCollectionFactory == null) throw new IllegalArgumentException(
				"fileRepositoryCollectionFactory is null");
		if (dataService == null) throw new IllegalArgumentException("dataservice is null");
		if (searchService == null) throw new IllegalArgumentException("seachservice is null");

		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.dataService = dataService;
		this.searchService = searchService;
	}

	@Override
	public EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction)
	{
		if (databaseAction != DatabaseAction.ADD) throw new IllegalArgumentException("Only ADD is supported");
		try
		{
			importVcf(source, 1000);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}

		EntityImportReport report = new EntityImportReport();
		return report;
	}

	@Override
	public EntitiesValidationReport validateImport(File file, RepositoryCollection source)
	{
		EntitiesValidationReport report = new EntitiesValidationReportImpl();

		for (String inEntityName : source.getEntityNames())
		{
			report.getSheetsImportable().put(inEntityName, !dataService.hasRepository(inEntityName));
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
		importVcf(repositoryCollection, 1000);
	}

	public void importVcf(RepositoryCollection repositoryCollection, int batchSize) throws IOException
	{
		ElasticsearchRepository sampleRepository = null;

		for (String inEntityName : repositoryCollection.getEntityNames())
		{
			Repository inRepository = repositoryCollection.getRepositoryByEntityName(inEntityName);

			if (dataService.hasRepository(inEntityName))
			{
				throw new MolgenisDataException("Can't overwrite existing " + inEntityName);
			}

			try
			{
				EntityMetaData entityMetaData = inRepository.getEntityMetaData();
				ElasticsearchRepository outRepository = new ElasticsearchRepository(entityMetaData, searchService);
				searchService.createMappings(entityMetaData, true, true, true, true);

				AttributeMetaData sampleAttribute = entityMetaData.getAttribute("SAMPLES");
				if (sampleAttribute != null)
				{
					sampleRepository = new ElasticsearchRepository(sampleAttribute.getRefEntity(), searchService);
					searchService.createMappings(sampleAttribute.getRefEntity(), true, true, true, true);
				}

				Iterator<Entity> inIterator = inRepository.iterator();
				try
				{
					List<Entity> sampleEntities = new ArrayList<Entity>();

					while (inIterator.hasNext())
					{
						Entity entity = inIterator.next();
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
										sampleEntities.clear();
									}
								}
							}
						}
					}
					outRepository.add(inRepository);
					if (sampleRepository != null) sampleRepository.add(sampleEntities);
				}

				finally
				{
					outRepository.close();
				}
				dataService.addRepository(outRepository);
				if (sampleRepository != null)
				{
					dataService.addRepository(sampleRepository);
				}
			}
			finally
			{
				inRepository.close();
			}
		}
	}

	@Override
	public int getOrder()
	{
		return 10;
	}

}
