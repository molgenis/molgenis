package org.molgenis.importer.vcf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.client.Client;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.ElasticsearchRepository;
import org.molgenis.data.elasticsearch.MappingManagerImpl;
import org.molgenis.elasticsearch.config.ElasticSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VcfImporterService
{
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final DataService dataService;
	private final ElasticSearchClient elasticSearchClient;

	@Autowired
	public VcfImporterService(FileRepositoryCollectionFactory fileRepositoryCollectionFactory, DataService dataService,
			ElasticSearchClient elasticSearchClient)
	{
		if (fileRepositoryCollectionFactory == null) throw new IllegalArgumentException(
				"fileRepositoryCollectionFactory is null");
		if (dataService == null) throw new IllegalArgumentException("data service is null");
		if (elasticSearchClient == null) throw new IllegalArgumentException("elasticSearchClient is null");
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.dataService = dataService;
		this.elasticSearchClient = elasticSearchClient;
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
				Client client = elasticSearchClient.getClient();
				String indexName = elasticSearchClient.getIndexName();
				EntityMetaData entityMetaData = inRepository.getEntityMetaData();
				ElasticsearchRepository outRepository = new ElasticsearchRepository(client, indexName, entityMetaData,
						dataService, new MappingManagerImpl());
				outRepository.create();
				AttributeMetaData sampleAttribute = entityMetaData.getAttribute("SAMPLES");
				if (sampleAttribute != null)
				{
					sampleRepository = new ElasticsearchRepository(client, indexName, sampleAttribute.getRefEntity(),
							dataService, new MappingManagerImpl());
					sampleRepository.create();
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
