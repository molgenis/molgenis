package org.molgenis.data.importer.vcf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.ElasticsearchRepository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.meta.ElasticsearchAttributeMetaDataRepository;
import org.molgenis.data.elasticsearch.meta.ElasticsearchEntityMetaDataRepository;
import org.molgenis.data.meta.AttributeMetaDataRepository;
import org.molgenis.data.meta.AttributeMetaDataRepositoryDecoratorFactory;
import org.molgenis.data.meta.EntityMetaDataRepository;
import org.molgenis.data.meta.EntityMetaDataRepositoryDecoratorFactory;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VcfImporterService
{
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

	public void importVcf(File vcfFile) throws IOException
	{
		importVcf(vcfFile, 1000);
	}

	public void importVcf(File vcfFile, int batchSize) throws IOException
	{
		RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
				.createFileRepositoryCollection(vcfFile);
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
									if (sampleEntities.size() > batchSize)
									{
										outRepository.add(sampleEntities);
										sampleEntities.clear();
									}
								}
							}
						}
					}
					outRepository.add(inRepository);
					outRepository.add(sampleEntities);
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
}
