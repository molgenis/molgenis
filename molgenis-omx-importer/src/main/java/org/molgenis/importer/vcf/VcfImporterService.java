package org.molgenis.importer.vcf;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

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

	public void importVcf(File vcfFile, String entityName) throws IOException
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
						dataService);
				outRepository.create();
				AttributeMetaData sampleAttribute = entityMetaData.getAttribute("SAMPLES");
				if (sampleAttribute != null)
				{
					sampleRepository = new ElasticsearchRepository(client, indexName, sampleAttribute.getRefEntity(),
							dataService);
					sampleRepository.create();
				}
				Iterator<Entity> inIterator = inRepository.iterator();
				try
				{
					while (inIterator.hasNext())
					{
						Entity entity = inIterator.next();
						outRepository.add(entity);
						if (sampleRepository != null)
						{
							sampleRepository.add(entity.getEntities("SAMPLES"));
						}
					}
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
